# -*- coding: UTF-8 -*-
__author__      = "Tonio Gsell <tgsell@tik.ee.ethz.ch>"
__copyright__   = "Copyright 2010, ETH Zurich, Switzerland, Tonio Gsell"
__license__     = "GPL"
__version__     = "$Revision$"
__date__        = "$Date$"
__id__          = "$Id$"
__source__      = "$URL$"

import os
import serial
import struct
from threading import Event

import BackLogMessage
from AbstractPlugin import AbstractPluginClass

DEFAULT_BACKLOG = True

class VaisalaWXT520PluginClass(AbstractPluginClass):
    '''
    This plugin sends weather sensor values from Vaisala WXT520 to GSN.
    '''

    '''
    _interval
    _sleeper
    _serial
    '''

    def __init__(self, parent, config):
        AbstractPluginClass.__init__(self, parent, config, DEFAULT_BACKLOG)
    
        self._ready = False
        self._sleeper = Event()
        self._stopped = False
        
        value = self.getOptionValue('poll_interval')
        if value is None:
            self._interval = None
        else:
            self._interval = float(value)
        
        self.info('interval: %s' % (self._interval,))
        
    
    def getMsgType(self):
        return BackLogMessage.VAISALA_WXT520_MESSAGE_TYPE
            
        
    def run(self):
        self.name = 'VaisalaWXT520Plugin-Thread'
        self.info('started')

        os.system('echo GPIO out clear > /proc/gpio/GPIO65')
        self.info('power down sensor...')

        self._sleeper.wait(5)
        if self._sleeper.isSet():
            return
        
        os.system('echo GPIO out set > /proc/gpio/GPIO65')
        self.info('power up sensor...')

        self._sleeper.wait(5)
        if self._sleeper.isSet():
            return

        self._serial = serial.Serial('/dev/ttyUSB0', 19200, bytesize=serial.EIGHTBITS, parity=serial.PARITY_NONE, stopbits=serial.STOPBITS_ONE, timeout=5)
        self._serial.open()
        self._serial.flushInput()
        self._serial.flushOutput()

        self._serial.write('?\r\n')
        self._id = self._serial.readline().strip()
        self.info('device address: %s' % (self._id,))
        if len(self._id) != 1 or not self._id.isalnum():
            self.error('received invalid device address')
        else:
            self._serial.write('%sXZM\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sXZ\r\n' % (self._id,))
            self._serial.readline()
    
            self._serial.write('%sXU\r\n' % (self._id,))
            output = self._serial.readline().strip()
            if output:
                self.info(output)
    
            self._serial.write('%sWU,R=1111110000000000,I=30\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sWU,A=3,G=1,U=K,D=0,N=W,F=4\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sWU\r\n')
            output = self._serial.readline().strip()
            if output:
                self.info(output)
            self._serial.write('%sTU,R=1111000000000000,I=30\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sTU,P=H,T=C\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sTU\r\n' % (self._id,))
            output = self._serial.readline().strip()
            if output:
                self.info(output)
            self._serial.write('%sRU,R=1111111100000000,I=30\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sRU,U=M,S=M,Z=M\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sRU,X=65535,Y=65535\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sRU\r\n')
            output = self._serial.readline().strip()
            if output:
                self.info(output)
            self._serial.write('%sSU,R=1111000000000000,I=30\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sSU,S=N,H=Y\r\n' % (self._id,))
            self._serial.readline()
            self._serial.write('%sSU\r\n' % (self._id,))
            output = self._serial.readline().strip()
            if output:
                self.info(output)
            
            self._ready = True
            
        if self._interval != None:
            while not self._stopped:
                self._sleeper.wait(self._interval)
                if self._sleeper.isSet():
                    continue
                self.action('')
            self.info('died')


    def action(self, parameters):
        if not self._ready:
            self.debug('weather station not ready for action')
            return
        else:
            try:
                send = False
                packet = []
                self._serial.write('%sR1\r\n' % (self._id,))
                line = self._serial.readline().strip()
                if line:
                    self.debug(line)
                    packet.append(line)
                    send = True
                else:
                    packet.append(None)
                
                self._serial.write('%sR2\r\n' % (self._id,))
                line = self._serial.readline().strip()
                if line:
                    self.debug(line)
                    packet.append(line)
                    send = True
                else:
                    packet.append(None)
                
                self._serial.write('%sR3\r\n' % (self._id,))
                line = self._serial.readline().strip()
                if line:
                    self.debug(line)
                    packet.append(line)
                    send = True
                else:
                    packet.append(None)
                
                self._serial.write('%sR5\r\n' % (self._id,))
                line = self._serial.readline().strip()
                if line:
                    self.debug(line)
                    packet.append(line)
                    send = True
                else:
                    packet.append(None)
                
                if send:
                    self.processMsg(self.getTimeStamp(), packet, self._priority, self._backlog)
                else:
                    self.error('Vaisala weather station not connected or wrongly configured')
            except Exception, e:
                self.exception(e)
        
        
    def isBusy(self):
        return False
        
        
    def needsWLAN(self):
        return False


    def stop(self):
        self._stopped = True
        self._sleeper.set()
        self._serial.close()
        self.info('stopped')
        
