/**
 *  Hubitat - Soundweb London DSP - Input Child Driver
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
        name: "Soundweb London Input",
        namespace: "TRATO",
        author: "VH",
    ) {
        capability "Switch"
        capability "AudioVolume"
        
        attribute "nodeAddress", "string"
        attribute "inputNumber", "number"
        attribute "mute", "string"
        
        
        command "setVolume", [[name: "volume", type: "NUMBER", description: "Volume level (0-100)"]]
        command "setNodeAddress", [[name: "nodeAddress*", type: "STRING", description: "Node address in hex"]]
        command "setInputNumber", [[name: "inputNumber*", type: "NUMBER", description: "Input number (1-12)"]]
    }
    
    preferences {
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
    }
}

def installed() {
    log.info "Soundweb London Input installed"
}

def updated() {
    log.info "Soundweb London Input updated"
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


def setInputNumber(inputNumber) {
    state.inputNumber = inputNumber
    sendEvent(name: "inputNumber", value: inputNumber)
}

private sendMuteCommand() {
    def inputNum = state.inputNumber
    if (!inputNum) {
        log.error "Input number not set"
        return
    }
    
    def nodeAddr = state.nodeAddress ?: parent?.settings?.nodeAddress ?: "DF47"
    
    def commandMap = [
        1: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D0 00 00 00 01 C4 03",
        2: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D1 00 00 00 01 C5 03",
        3: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D2 00 00 00 01 C6 03",
        4: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D3 00 00 00 01 C7 03",
        5: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D0 00 00 00 01 C7 03",
        6: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D1 00 00 00 01 C6 03",
        7: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D2 00 00 00 01 C5 03",
        8: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D3 00 00 00 01 C4 03",
        9: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D0 00 00 00 01 C6 03",
        10: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D1 00 00 00 01 C7 03",
        11: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D2 00 00 00 01 C4 03",
        12: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D3 00 00 00 01 C5 03"
    ]
    
    def hexCommand = commandMap[inputNum]
    if (hexCommand) {
        parent.sendHexCommand(hexCommand.replaceAll(" ", ""))
        sendEvent(name: "switch", value: "on")
        log.info "Muted input ${inputNum}"

    }

}



private sendUnmuteCommand() {

    def inputNum = state.inputNumber
    if (!inputNum) {
        log.error "Input number not set"
        return
    }
    
    def nodeAddr = state.nodeAddress ?: parent?.settings?.nodeAddress ?: "DF47"    
    
    def commandMap = [
        1: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D0 00 00 00 00 C5 03",
        2: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D1 00 00 00 00 C4 03",
        3: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D2 00 00 00 00 C7 03",
        4: "02 88 ${nodeAddr} 1B 83 00 00 01 07 D3 00 00 00 00 C6 03",
        5: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D0 00 00 00 00 C6 03",
        6: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D1 00 00 00 00 C7 03",
        7: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D2 00 00 00 00 C4 03",
        8: "02 88 ${nodeAddr} 1B 83 00 00 1B 82 07 D3 00 00 00 00 C5 03",
        9: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D0 00 00 00 00 C7 03",
        10: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D1 00 00 00 00 C6 03",
        11: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D2 00 00 00 00 C5 03",
        12: "02 88 ${nodeAddr} 1B 83 00 00 1B 83 07 D3 00 00 00 00 C4 03"
    ]
    
    def hexCommand = commandMap[inputNum]
    if (hexCommand) {
        parent.sendHexCommand(hexCommand.replaceAll(" ", ""))
        sendEvent(name: "switch", value: "off")
        log.info "Unmuted input ${inputNum}"

    }
}

def setVolume(volume) {
    try {
        // Safely convert volume to a float
        def volumeNum = volume.toString().toFloat()
        
        // Validate volume range
        volumeNum = Math.max(0, Math.min(100, volumeNum))
        
        // Get and validate input number
        def inputNum = safeToInteger(state.inputNumber)
        if (inputNum == null || inputNum < 1 || inputNum > 12) {
            log.error "Invalid input number: ${state.inputNumber}"
            return
        }
        
        // Get node address
        def nodeAddr = state.nodeAddress ?: parent?.settings?.nodeAddress ?: "DF47"
        
        // Get the hex command - now passing nodeAddr correctly
        def hexCommand = getVolumeHexCommand(inputNum, volumeNum, nodeAddr)
        if (!hexCommand) {
            log.error "Failed to generate command for input ${inputNum} at ${volumeNum}%"
            return
        }
        
        // Send the command
        if (logEnable) log.debug "Sending volume command: ${hexCommand} for input ${inputNum} at ${volumeNum}% "
        parent.sendHexCommand(hexCommand.replaceAll(" ", ""))
        sendEvent(name: "volume", value: volumeNum.round(1), unit: "%")
        log.info "Sending volume for input ${inputNum} at ${volumeNum}% "
        
    } catch (Exception e) {
        log.error "Error in setVolume: ${e}"
    }
}

private Integer safeToInteger(value) {
    try {
        return value?.toInteger()
    } catch (e) {
        return null
    }
}



private String getVolumeHexCommand(Integer inputNum, Float volume, String nodeAddr) {
    // Create a map of all volume commands from the spreadsheet
    def volumeCommands = [
        // Input 1
        1: [
            0:    "02 8D DF 47 1B 83 00 00 01 00 04 00 00 00 00 13 03",
            12.5: "02 8D DF 47 1B 83 00 00 01 00 04 00 0C 80 00 9F 03",
            25:   "02 8D DF 47 1B 83 00 00 01 00 04 00 19 00 00 0A 03",
            37.5: "02 8D DF 47 1B 83 00 00 01 00 04 00 25 80 00 B6 03",
            50:   "02 8D DF 47 1B 83 00 00 01 00 04 00 32 00 00 21 03",
            62.5: "02 8D DF 47 1B 83 00 00 01 00 04 00 3E 80 00 AD 03",
            75:   "02 8D DF 47 1B 83 00 00 01 00 04 00 4B 00 00 58 03",
            87.5: "02 8D DF 47 1B 83 00 00 01 00 04 00 57 80 00 C4 03",
            100:  "02 8D DF 47 1B 83 00 00 01 00 04 00 64 00 00 77 03"
        ],
        // Input 2
        2: [
            0:    "02 8D DF 47 1B 83 00 00 01 00 0A 00 00 00 00 1D 03",
            12.5: "02 8D DF 47 1B 83 00 00 01 00 0A 00 0C 80 00 91 03",
            25:   "02 8D DF 47 1B 83 00 00 01 00 0A 00 19 00 00 04 03",
            37.5: "02 8D DF 47 1B 83 00 00 01 00 0A 00 25 80 00 B8 03",
            50:   "02 8D DF 47 1B 83 00 00 01 00 0A 00 32 00 00 2F 03",
            62.5: "02 8D DF 47 1B 83 00 00 01 00 0A 00 3E 80 00 A3 03",
            75:   "02 8D DF 47 1B 83 00 00 01 00 0A 00 4B 00 00 56 03",
            87.5: "02 8D DF 47 1B 83 00 00 01 00 0A 00 57 80 00 CA 03",
            100:  "02 8D DF 47 1B 83 00 00 01 00 0A 00 64 00 00 79 03"
        ],
        // Input 3
        3: [
            0:    "02 8D DF 47 1B 83 00 00 01 00 10 00 00 00 00 07 03",
            12.5: "02 8D DF 47 1B 83 00 00 01 00 10 00 0C 80 00 8B 03",
            25:   "02 8D DF 47 1B 83 00 00 01 00 10 00 19 00 00 1E 03",
            37.5: "02 8D DF 47 1B 83 00 00 01 00 10 00 25 80 00 A2 03",
            50:   "02 8D DF 47 1B 83 00 00 01 00 10 00 32 00 00 35 03",
            62.5: "02 8D DF 47 1B 83 00 00 01 00 10 00 3E 80 00 B9 03",
            75:   "02 8D DF 47 1B 83 00 00 01 00 10 00 4B 00 00 4C 03",
            87.5: "02 8D DF 47 1B 83 00 00 01 00 16 00 57 80 00 D6 03",
            100:  "02 8D DF 47 1B 83 00 00 01 00 10 00 64 00 00 63 03"
        ],
        // Input 4
        4: [
            0:    "02 8D DF 47 1B 83 00 00 01 00 16 00 00 00 00 01 03",
            12.5: "02 8D DF 47 1B 83 00 00 01 00 16 00 0C 80 00 8D 03",
            25:   "02 8D DF 47 1B 83 00 00 01 00 16 00 19 00 00 18 03",
            37.5: "02 8D DF 47 1B 83 00 00 01 00 16 00 25 80 00 A4 03",
            50:   "02 8D DF 47 1B 83 00 00 01 00 16 00 32 00 00 33 03",
            62.5: "02 8D DF 47 1B 83 00 00 01 00 16 00 3E 80 00 BF 03",
            75:   "02 8D DF 47 1B 83 00 00 01 00 16 00 4B 00 00 4A 03",
            87.5: "02 8D DF 47 1B 83 00 00 01 00 16 00 57 80 00 D6 03",
            100:  "02 8D DF 47 1B 83 00 00 01 00 16 00 64 00 00 65 03"
        ],
        // Input 5
        5: [
            0:    "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 00 00 00 10 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 0C 80 00 9C 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 19 00 00 09 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 25 80 00 B5 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 32 00 00 22 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 3E 80 00 AE 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 4B 00 00 5B 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 57 80 00 C7 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 82 00 04 00 64 00 00 74 03"
        ],
        // Input 6
        6: [
            0:    "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 00 00 00 1E 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 0C 80 00 92 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 19 00 00 07 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 25 80 00 BB 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 32 00 00 2C 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 3E 80 00 A0 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 4B 00 00 55 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 57 80 00 C8 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 82 00 0A 00 64 00 00 7A 03"
        ],
        // Input 7
        7: [
            0:    "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 00 00 00 04 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 0C 80 00 88 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 19 00 00 1D 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 25 80 00 A1 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 32 00 00 36 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 3E 80 00 BA 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 4B 00 00 4F 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 57 80 00 D3 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 82 00 10 00 64 00 00 60 03"
        ],
        // Input 8
        8: [
            0:    "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 00 00 00 1B 82 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 0C 80 00 8E 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 19 00 00 1B 9B 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 25 80 00 A7 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 32 00 00 30 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 3E 80 00 BC 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 4B 00 00 49 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 57 80 00 C6 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 82 00 16 00 64 00 00 66 03"
        ],
        // Input 9
        9: [
            0:    "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 00 00 00 11 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 0C 80 00 9D 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 19 00 00 08 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 25 80 00 B4 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 32 00 00 23 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 3E 80 00 AF 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 4B 00 00 5A 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 57 80 00 C6 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 83 00 04 00 64 00 00 75 03"
        ],
        // Input 10
        10: [
            0:    "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 00 00 00 1F 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 0C 80 00 93 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 19 00 00 1B 86 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 25 80 00 BA 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 32 00 00 2D 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 3E 80 00 A1 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 4B 00 00 54 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 57 80 00 C8 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 83 00 0A 00 64 00 00 7B 03"
        ],
        // Input 11
        11: [
            0:    "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 00 00 00 05 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 0C 80 00 89 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 19 00 00 1C 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 25 80 00 A0 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 32 00 00 37 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 3E 80 00 BB 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 4B 00 00 4E 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 57 80 00 D2 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 83 00 10 00 64 00 00 61 03"
        ],
        // Input 12
        12: [
            0:    "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 00 00 00 1B 83 03",
            12.5: "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 0C 80 00 8F 03",
            25:   "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 19 00 00 1A 03",
            37.5: "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 25 80 00 A6 03",
            50:   "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 32 00 00 31 03",
            62.5: "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 3E 80 00 BD 03",
            75:   "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 4B 00 00 48 03",
            87.5: "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 57 80 00 D4 03",
            100:  "02 8D DF 47 1B 83 00 00 1B 83 00 16 00 64 00 00 67 03"
        ]
    ]
    
    
    try {
        // Get available volumes for this input
        def availableVolumes = volumeCommands[inputNum]?.keySet()?.toList()?.sort()
        
        if (!availableVolumes) {
            log.error "No volume commands defined for input ${inputNum}"
            return null
        }
        
        // Find closest volume level
        def closestVolume = availableVolumes.min { Math.abs(it - volume) }
        
        // Get command template
        def commandTemplate = volumeCommands[inputNum]?.get(closestVolume)
        if (!commandTemplate) {
            log.error "No command template found for input ${inputNum} at ${closestVolume}%"
            return null
        }
        
        // Replace node address and return
        return commandTemplate.replace("DF 47", nodeAddr)
        
    } catch (Exception e) {
        log.error "Error in getVolumeHexCommand: ${e}"
        return null
    }
}


def volumeUp() {
    def currentVolume = device.currentValue("volume") ?: 0
    def volumeSteps = [0.0, 12.5, 25.0, 37.5, 50.0, 62.5, 75.0, 87.5, 100.0]
    
    // Find the next highest step
    def nextVolume = volumeSteps.find { it > currentVolume } ?: 100.0
    
    // If already at max volume, don't change
    if (nextVolume <= currentVolume) {
        log.info "Already at maximum volume (${currentVolume}%)"
        return
    }
    
    if (logEnable) log.debug "Increasing volume from ${currentVolume}% to ${nextVolume}%"
    setVolume(nextVolume)
}

def volumeDown() {
    def currentVolume = device.currentValue("volume") ?: 0
    def volumeSteps = [0.0, 12.5, 25.0, 37.5, 50.0, 62.5, 75.0, 87.5, 100.0]
    
    // Reverse the steps to find the next lowest
    def reversedSteps = volumeSteps.reverse()
    def nextVolume = reversedSteps.find { it < currentVolume } ?: 0.0
    
    // If already at min volume, don't change
    if (nextVolume >= currentVolume) {
        log.info "Already at minimum volume (${currentVolume}%)"
        return
    }
    
    if (logEnable) log.debug "Decreasing volume from ${currentVolume}% to ${nextVolume}%"
    setVolume(nextVolume)
}


private getVolumeParamIdForInput(inputNum) {
    // Parameter IDs for volume control (matches spreadsheet)
    def baseParam = [
        "01", "01", "01", "01",  // Inputs 1-4
        "1B 82", "1B 82", "1B 82", "1B 82",  // Inputs 5-8
        "1B 83", "1B 83", "1B 83", "1B 83"   // Inputs 9-12
    ]c
    def subParam = [
        "00 04", "00 0A", "00 10", "00 16",  // Inputs 1-4
        "00 04", "00 0A", "00 10", "00 16",  // Inputs 5-8
        "00 04", "00 0A", "00 10", "00 16"   // Inputs 9-12
    ]
    
    return "${baseParam[inputNum-1]} ${subParam[inputNum-1]}"
}

private calculateChecksum(hexBody) {
    def bytes = hexBody.decodeHex()
    def checksum = 0
    
    bytes.each { byteVal ->
        checksum = checksum ^ byteVal
    }
    
    return String.format("%02X", checksum)
}


