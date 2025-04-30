/**
 *  Hubitat - Soundweb London DSP - Output Child Driver
 *  
 *
 *  Copyright 2025 VH/TRATO
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *        
 *  Version 1.0 - 30/4/2025 - Beta 1.0
 *                 
 *                 
 *                 
 *                 
 *                
 */
metadata {
    definition (
        name: "Soundweb London Output",
        namespace: "TRATO",
        author: "VH",
    ) {
        capability "Switch"
        capability "AudioVolume"
        
        attribute "nodeAddress", "string"
        attribute "outputNumber", "number"
        attribute "mute", "string"
        
        //command "setVolume", [[name: "volume", type: "NUMBER", description: "Volume level (0-100)"]]
        command "setNodeAddress", [[name: "nodeAddress*", type: "STRING", description: "Node address in hex"]]
        command "setOutputNumber", [[name: "outputNumber*", type: "NUMBER", description: "Output number (1-8)"]]
    }
    
    preferences {
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
    }
}

def installed() {
    log.info "Soundweb London Output installed"
}

def updated() {
    log.info "Soundweb London Output updated"
}

def on() {
    sendMuteCommand()
    sendEvent(name: "mute", value: "muted")    
}

def off() {
    sendUnmuteCommand()
    sendEvent(name: "mute", value: "unmuted")
}

def mute() {
    on()    
}

def unmute() {
    off()
}

def setNodeAddress(nodeAddress) {
    state.nodeAddress = nodeAddress
    sendEvent(name: "nodeAddress", value: nodeAddress)
}

def setOutputNumber(outputNumber) {
    state.outputNumber = outputNumber
    sendEvent(name: "outputNumber", value: outputNumber)
}

private sendMuteCommand() {
    def outputNum = state.outputNumber
    if (!outputNum) {
        log.error "Output number not set"
        return
    }
    
    def nodeAddr = state.nodeAddress ?: parent?.settings?.nodeAddress ?: "DF47"
    
    def commandMap = [
        1: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D0 00 00 00 01 C1 03",
        2: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D1 00 00 00 01 C0 03",
        3: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D2 00 00 00 01 C3 03",
        4: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D3 00 00 00 01 C2 03",
        5: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D0 00 00 00 01 C0 03",
        6: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D1 00 00 00 01 C1 03",
        7: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D2 00 00 00 01 C2 03",
        8: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D3 00 00 00 01 C3 03"
    ]
    
    def hexCommand = commandMap[outputNum]
    if (hexCommand) {
        parent.sendHexCommand(hexCommand.replaceAll(" ", ""))
        sendEvent(name: "switch", value: "on")
        log.info "Muted output ${outputNum}"
    }
}

private sendUnmuteCommand() {
    def outputNum = state.outputNumber
    if (!outputNum) {
        log.error "Output number not set"
        return
    }
    
    def nodeAddr = state.nodeAddress ?: parent?.settings?.nodeAddress ?: "DF47"    
    
    def commandMap = [
        1: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D0 00 00 00 00 C0 03",
        2: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D1 00 00 00 00 C1 03",
        3: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D2 00 00 00 00 C2 03",
        4: "02 88 ${nodeAddr} 1B 83 00 00 04 07 D3 00 00 00 00 C3 03",
        5: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D0 00 00 00 00 C1 03",
        6: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D1 00 00 00 00 C0 03",
        7: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D2 00 00 00 00 C3 03",
        8: "02 88 ${nodeAddr} 1B 83 00 00 05 07 D3 00 00 00 00 C2 03"
    ]
    
    def hexCommand = commandMap[outputNum]
    if (hexCommand) {
        parent.sendHexCommand(hexCommand.replaceAll(" ", ""))
        sendEvent(name: "switch", value: "off")
        log.info "Unmuted output ${outputNum}"
    }
}

def setVolume(volume) {
   
}

private Integer safeToInteger(value) {
    try {
        return value?.toInteger()
    } catch (e) {
        return null
    }
}


def volumeUp() {
   
}

def volumeDown() {
   
}