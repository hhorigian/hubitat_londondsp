/**
 *  Hubitat - Soundweb London DSP - Controller Driver
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
        name: "Soundweb London Controller",
        namespace: "TRATO",
        author: "VH",
    ) {
        capability "Initialize"
        command "deleteChildDevices"
        command "connect"
        command "disconnect"
        command "reconnect"  
        command "sendManualHexCommand", ["string"]        
        command "subscribeAll" // New command to subscribe to all parameters
        
        attribute "connectionStatus", "string"
        
    }
    
    preferences {
        input name: "deviceIP", type: "text", title: "Device IP Address", required: true
        input name: "nodeAddress", type: "text", title: "Node Address (hex, e.g., DF47)", required: true
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
    }
}

def installed() {
    log.info "Soundweb London Controller installed"
    initialize()
}

def updated() {
    log.info "Soundweb London Controller updated"
    initialize()
}

def initialize() {
    log.info "Initializing Soundweb London Controller"
    
    try {
        interfaces.rawSocket.connect("${settings.deviceIP}", 1023)
        sendEvent(name: "connectionStatus", value: "connected")
        log.info "Connected to Soundweb London device at ${settings.deviceIP}:1023"
        createChildDevicesIfNeeded()

        // Subscribe to parameters after a short delay to ensure connection is stable
        runIn(3, "subscribeAll")
        
    } catch (Exception e) {
        log.error "Connection failed: ${e}"
        sendEvent(name: "connectionStatus", value: "disconnected")
    }
}

def uninstalled() {
    interfaces.rawSocket.close()
}


//// CONNECTION 

def connect() {
    try {
        // Close any existing connection
        disconnect()
        
        // Get IP and port from preferences
        def ip = settings.deviceIP
        def port = settings.port ?: 1023
        
        if (!ip) {
            log.error "IP address not configured"
            sendEvent(name: "connection", value: "disconnected")
            return
        }
        
        log.info "Connecting to Soundweb London DSP at ${ip}:${port}"
        
        // Update device attributes
        sendEvent(name: "ipAddress", value: ip)
        sendEvent(name: "connection", value: "connecting")
        
        // Open TCP socket with timeout
        interfaces.rawSocket.connect(ip, port.toInteger(), byteInterface: true, timeout: 5000)
        
        // Store connection time
        state.connectionAttemptTime = now()
        
        // Schedule connection verification
        runIn(5, verifyConnection)
        
    } catch (Exception e) {
        log.error "Connection failed: ${e.message}"
        sendEvent(name: "connection", value: "disconnected")
        runIn(30, reconnect)
    }
}

def verifyConnection() {
    // Send a test message to verify connection
    def testMessage = [0x02, 0x89, 0x00, 0x01, 0x03, 0x00, 0x00, 0x01, 0x07, 0xD0] as byte[]
    def checksum = calculateChecksum(testMessage)
    def messageWithChecksum = testMessage + [checksum, 0x03] as byte[]
    def finalMessage = applyByteSubstitution(messageWithChecksum)
    
    try {
        interfaces.rawSocket.sendMessage(finalMessage)
        log.debug "Sent test message to verify connection"
        sendEvent(name: "connection", value: "connected")
        log.info "Connection verified"
        subscribeToParameters()
    } catch (Exception e) {
        log.warn "Connection verification failed: ${e.message}"
        sendEvent(name: "connection", value: "disconnected")
        runIn(30, reconnect)
    }
}

def disconnect() {
    try {
        interfaces.rawSocket.close()
        sendEvent(name: "connection", value: "disconnected")
        log.info "Disconnected from Soundweb London DSP"
    } catch (Exception e) {
        log.error "Error disconnecting: ${e.message}"
    }
}

def reconnect() {
    log.info "Attempting to reconnect..."
    disconnect()
    connect()
}

def socketStatus(String status) {
    log.info "Socket status: ${status}"
    sendEvent(name: "connectionStatus", value: status)
    
    if (status == "disconnected") {
        runIn(10, initialize) // Attempt to reconnect
    }
}

/// CHILDREN

def createChildDevicesIfNeeded() {
    // Check if child devices already exist
    def existingChildren = getChildDevices()
    
    // Create input child devices if missing (1-12)
    (1..12).each { inputNum ->
        def childDni = "${device.deviceNetworkId}-INPUT${inputNum}"
        if (!existingChildren.find { it.deviceNetworkId == childDni }) {
            def child = addChildDevice("TRATO", "Soundweb London Input", childDni, 
                                    [label: "${device.displayName} Input ${inputNum}", 
                                     name: "Input ${inputNum}"])
            child.setNodeAddress(settings.nodeAddress)
            child.setInputNumber(inputNum)
            log.info "Created input child device #${inputNum}"
        }
    }
    
    // Create output child devices if missing (1-8)
    (1..8).each { outputNum ->
        def childDni = "${device.deviceNetworkId}-OUTPUT${outputNum}"
        if (!existingChildren.find { it.deviceNetworkId == childDni }) {
            def child = addChildDevice("TRATO", "Soundweb London Output", childDni, 
                                    [label: "${device.displayName} Output ${outputNum}", 
                                     name: "Output ${outputNum}"])
            child.setNodeAddress(settings.nodeAddress)
            child.setOutputNumber(outputNum)
            log.info "Created output child device #${outputNum}"
        }
    }
}

def deleteChildDevices() {
    getChildDevices().each { deleteChildDevice(it.deviceNetworkId) }
    log.info "Deleted all child devices"
}

def sendManualHexCommand(String hexCommand) {
    if (!hexCommand) {
        log.warn "Empty hex command received"
        return
    }
    
    try {
        // Clean the command by removing all spaces and non-hex characters
        def cleanCommand = hexCommand.replaceAll("[^0-9A-Fa-f]", "").toUpperCase()
        
        if (logEnable) {
            log.debug "Sending manual hex command: ${cleanCommand}"
        } else {
            log.info "Sending manual hex command (enable debug logging to see full command)"
        }
        
        // Send the raw command
        sendHexCommand(cleanCommand)
        
    } catch (Exception e) {
        log.error "Failed to send manual hex command: ${e.message}"
    }
}

def sendHexCommand(String hexCommand) {
    // Remove all existing spaces first to ensure clean formatting
    def cleanCommand = hexCommand.replaceAll(" ", "")
    
    // Add space between each byte, but not after the last one
    def spacedCommand = cleanCommand.replaceAll("(.{2})", "\$1 ").trim()
    
    if (logEnable) log.debug "Sending command: [${spacedCommand}]"
    
    try {
        log.info "Sending command: " + spacedCommand
        interfaces.rawSocket.sendMessage(spacedCommand)
    } catch (Exception e) {
        log.error "Failed to send command: ${e}"
        sendEvent(name: "connectionStatus", value: "disconnected")
    }
}

private calculateChecksum(hexBody) {
    def bytes = hexBody.decodeHex()
    def checksum = 0
    
    bytes.each { byteVal ->
        checksum = checksum ^ byteVal
    }
    
    return String.format("%02X", checksum)
}


///// PARSE /////

def parse(String message) {
    if (logEnable) log.debug "Received message: ${message}"
    
    // Parse mute status updates
    if (message.startsWith("0288") || message.startsWith("0289")) {
        parseMuteStatus(message)
    }
    // Parse volume status updates
    else if (message.startsWith("028D") || message.startsWith("028E")) {
        parseVolumeStatus(message)
    }
}


///// PARSE - MUTE ///

private parseMuteStatus(String message) {
    if (message.length() < 32) {
        if (logEnable) log.warn "Received incomplete mute status message: ${message}"
        return
    }

    try {
        log.debug "=== FULL MESSAGE ANALYSIS ==="
        log.debug "Complete message: ${message}"
        log.debug "Message length: ${message.length()} chars"
        
        // Extract key components
        def nodeAddr = message.substring(4, 8)
        def paramSection = message.substring(16, 24)
        def valueBytes = message.substring(24, 32)
        
        // Determine mute value position based on parameter section
        def muteValue
        if (paramSection.startsWith("01") || paramSection.startsWith("04") || paramSection.startsWith("05")) {
            // Standard outputs (1-8) and inputs 1-4 - mute status at bytes 4-6
            muteValue = valueBytes.substring(4, 6)
        } else if (paramSection.startsWith("1B82") || paramSection.startsWith("1B83")) {
            // Inputs 5-12 - mute status at bytes 6-8
            muteValue = valueBytes.substring(6, 8)
        } else {
            log.debug "Unknown parameter section format"
            return
        }
        
        log.debug "Node Address: ${nodeAddr}"
        log.debug "Parameter Section: ${paramSection}"
        log.debug "Value Bytes: ${valueBytes}"
        log.debug "Mute Status Value: ${muteValue}"

        // Try to parse as output command first (since outputs are more common)
        def outputNum = parseOutputMuteCommand(paramSection)
        if (outputNum != null) {
            def isMuted = muteValue == "01"
            log.debug "Identified: OUTPUT ${outputNum} ${isMuted ? 'MUTED' : 'UNMUTED'}"
            updateOutputDevice(outputNum, isMuted)
            return
        }
        
        // If not an output, try to parse as input command
        def inputNum = parseInputMuteCommand(paramSection)
        if (inputNum != null) {
            def isMuted = muteValue == "01"
            log.debug "Identified: INPUT ${inputNum} ${isMuted ? 'MUTED' : 'UNMUTED'}"
            updateInputDevice(inputNum, isMuted)
            return
        }
        
        log.debug "Not a recognized mute command"
        
    } catch (Exception e) {
        log.error "Error parsing mute status: ${e.message}"
    }
}

private Integer parseInputMuteCommand(String paramSection) {
    // Create mapping table for input mute commands
    def inputCommandMap = [
        // Inputs 1-4
        "0107D0": 1,
        "0107D1": 2,
        "0107D2": 3,
        "0107D3": 4,
        
        // Inputs 5-8
        "1B8207D0": 5,
        "1B8207D1": 6,
        "1B8207D2": 7,
        "1B8207D3": 8,
        
        // Inputs 9-12
        "1B8307D0": 9,
        "1B8307D1": 10,
        "1B8307D2": 11,
        "1B8307D3": 12
    ]

    // Find matching parameter pattern
    def inputNum = inputCommandMap.find { paramSection.startsWith(it.key) }?.value
    
    return inputNum
}

private Integer parseOutputMuteCommand(String paramSection) {
    // Mapping for output mute commands
    def outputCommandMap = [
        // Outputs 1-4
        "0407D0": 1,
        "0407D1": 2,
        "0407D2": 3,
        "0407D3": 4,
        
        // Outputs 5-8
        "0507D0": 5,
        "0507D1": 6,
        "0507D2": 7,
        "0507D3": 8
    ]

    // Find matching parameter pattern
    def outputNum = outputCommandMap.find { paramSection.startsWith(it.key) }?.value
    
    return outputNum
}

private updateInputDevice(inputNum, isMuted) {
    def child = getChildDevices().find { it.deviceNetworkId.endsWith("-INPUT${inputNum}") }
    if (child) {
        child.sendEvent(name: "switch", value: isMuted ? "on" : "off")
        child.sendEvent(name: "mute", value: isMuted ? "muted" : "unmuted")
        log.info "INPUT ${inputNum} updated to ${isMuted ? 'muted' : 'unmuted'}"
    }
}

private updateOutputDevice(outputNum, isMuted) {
    def child = getChildDevices().find { it.deviceNetworkId.endsWith("-OUTPUT${outputNum}") }
    if (child) {
        child.sendEvent(name: "switch", value: isMuted ? "on" : "off")
        child.sendEvent(name: "mute", value: isMuted ? "muted" : "unmuted")
        log.info "OUTPUT ${outputNum} updated to ${isMuted ? 'muted' : 'unmuted'}"
    }
}

private extractIONumber(String parameterId, String subParam) {
    try {
        // Input mutes (parameter ID 0107, subParam D0-D3)
        if (parameterId == "0107") {
            def inputIndex = Integer.parseInt(subParam.substring(1, 2), 16) // Get the 0 from D0
            return [inputIndex + 1, true]  // D0=1, D1=2, D2=3, D3=4
        }
        // Output mutes (parameter ID 0407, subParam D0-D3)
        else if (parameterId == "0407") {
            def outputIndex = Integer.parseInt(subParam.substring(1, 2), 16)
            return [outputIndex + 1, false] 
        }
    } catch (Exception e) {
        log.error "Error extracting IO number: ${e.message}"
    }
    return [null, false]
}



////// PARSE - VOLUME ////

// Initialize volumeSteps as a class field
def getVolumeSteps() { 
    return [0.0, 12.5, 25.0, 37.5, 50.0, 62.5, 75.0, 87.5, 100.0] 
}

def parseVolumeStatus(String message) {
    if (logEnable) log.debug "Starting volume message parsing with input: ${message}"
    
    try {
        // Step 1: Reverse byte substitution
        def cleanedMessage = reverseByteSubstitution(message)
        
        // Step 2: Verify basic message structure
        if (cleanedMessage.length() < 32) {
            if (logEnable) log.warn "Received incomplete volume message: ${cleanedMessage}"
            return
        }
        
        // Step 3: Extract key components
        def messageType = cleanedMessage.substring(0, 4)
        def nodeAddress = cleanedMessage.substring(4, 8)
        def virtualDevice = cleanedMessage.substring(8, 10)
        def objectId = cleanedMessage.substring(10, 16)
        def parameterId = cleanedMessage.substring(16, 20)
        def percentValueHex = cleanedMessage.substring(20, 28)
        
        // Step 4: Get input number
        def inputNum = getInputNumberFromParameters(objectId, parameterId)
        if (!inputNum) {
            if (logEnable) log.debug "Unrecognized parameter combination: Object ${objectId}, Param ${parameterId}"
            return
        }
        
        // Step 5: Convert hex to percentage
        def rawPercentage = convertHexPercentageToValue(percentValueHex)
        if (rawPercentage == null) return
        
        // Step 6: Special handling for 0%
        def snappedPercent
        if (rawPercentage == 0.0) {
            snappedPercent = 0.0
        } else {
            // Snap to closest higher volume step for non-zero values
            def steps = getVolumeSteps()
            snappedPercent = steps.find { it >= rawPercentage } ?: steps.last()
            
            // For values between steps, find the next higher one
            if (snappedPercent < rawPercentage) {
                def higherIndex = steps.indexOf(snappedPercent) + 1
                snappedPercent = higherIndex < steps.size() ? steps[higherIndex] : steps.last()
            }
        }
        
        def volumeDb = convertPercentToDb(snappedPercent)
        
        // Enhanced logging
        if (logEnable) {
            log.debug """
            |=== VOLUME PROCESSING ===
            |Hex Value: ${percentValueHex}
            |Raw Percentage: ${rawPercentage.round(2)}%
            |Snapped To: ${snappedPercent}%
            |dB Value: ${volumeDb} dB
            |Input: ${inputNum}
            |"""
            .stripMargin()
        }
        
        // Step 7: Update device
        updateInputVolumeDevice(inputNum, snappedPercent, volumeDb)
        
    } catch (Exception e) {
        log.error "Error parsing volume status: ${e.message}"
    }
}

private Double snapToVolumeStep(Double percent) {
    // Access the class-level volumeSteps
    return this.volumeSteps.min { Math.abs(it - percent) }
}

private String reverseByteSubstitution(String message) {
    return message.replaceAll("1B82", "02")
                .replaceAll("1B83", "03")
                .replaceAll("1B86", "06")
                .replaceAll("1B95", "15")
                .replaceAll("1B9B", "1B")
}

private Integer getInputNumberFromParameters(String objectId, String parameterId) {
    def parameterMap = [
        "0000010004": 1, "000001000A": 2, "0000010010": 3, "0000010016": 4,
        "0000020004": 5, "000002000A": 6, "0000020010": 7, "0000020016": 8,
        "0000030004": 9, "000003000A": 10, "0000030010": 11, "0000030016": 12
    ]
    return parameterMap["${objectId}${parameterId}"]
}

private Double convertHexPercentageToValue(String percentValueHex) {
    try {
        long rawValue = Long.parseLong(percentValueHex, 16)
        if (rawValue > 0x7FFFFFFF) rawValue -= 0x100000000
        return (rawValue / 65536.0)
    } catch (Exception e) {
        log.error "Hex conversion failed for ${percentValueHex}: ${e.message}"
        return null
    }
}

private BigDecimal convertPercentToDb(Double percent) {
    try {
        if (percent <= 0) return new BigDecimal(-80.0)
        
        double dbValue
        if (percent <= 73.73) {
            dbValue = 20 * Math.log10(percent / 73.73)
        } else {
            dbValue = (percent - 73.73) * (10.0 / 26.27)
        }
        return new BigDecimal(dbValue).setScale(1, BigDecimal.ROUND_HALF_UP)
    } catch (Exception e) {
        log.error "dB conversion failed for ${percent}%: ${e.message}"
        return null
    }
}

private updateInputVolumeDevice(inputNum, volumePercent, volumeDb) {
    def child = getChildDevices().find { it.deviceNetworkId.endsWith("-INPUT${inputNum}") }
    if (child) {
        try {
            //child.sendEvent(name: "volume", value: volumeDb, unit: "dB")
            child.sendEvent(name: "volume", value: volumePercent, unit: "%")
            //child.sendEvent(name: "level", value: volumePercent, unit: "%")
            log.info "INPUT ${inputNum} → ${volumeDb} dB (${volumePercent}%)"
        } catch (Exception e) {
            log.error "Update failed for input ${inputNum}: ${e.message}"
        }
    }
}


////// SUBSCRIPTION /////

def subscribeAll() {
    log.info "Subscribing to all parameters"
    subscribeToInputMutes()
    pauseExecution(500)
    subscribeToOutputMutes()
    pauseExecution(500)    
    subscribeToInputVolumes()
}

private subscribeToInputMutes() {
    def nodeAddr = settings.nodeAddress ?: "DF47"
    
    // Input Mute Subscriptions (1-12)
    def inputMuteSubscriptions = [
        "02 89 ${nodeAddr} 1B 83 00 00 01 07 D0 00 00 00 00 C4 03", // Input 1
        "02 89 ${nodeAddr} 1B 83 00 00 01 07 D1 00 00 00 00 C5 03", // Input 2
        "02 89 ${nodeAddr} 1B 83 00 00 01 07 D2 00 00 00 00 C6 03", // Input 3
        "02 89 ${nodeAddr} 1B 83 00 00 01 07 D3 00 00 00 00 C7 03", // Input 4
        "02 89 ${nodeAddr} 1B 83 00 00 1B 82 07 D0 00 00 00 00 C7 03", // Input 5
        "02 89 ${nodeAddr} 1B 83 00 00 1B 82 07 D1 00 00 00 00 C6 03", // Input 6
        "02 89 ${nodeAddr} 1B 83 00 00 1B 82 07 D2 00 00 00 00 C5 03", // Input 7
        "02 89 ${nodeAddr} 1B 83 00 00 1B 82 07 D3 00 00 00 00 C4 03", // Input 8
        "02 89 ${nodeAddr} 1B 83 00 00 1B 83 07 D0 00 00 00 00 C6 03", // Input 9
        "02 89 ${nodeAddr} 1B 83 00 00 1B 83 07 D1 00 00 00 00 C7 03", // Input 10
        "02 89 ${nodeAddr} 1B 83 00 00 1B 83 07 D2 00 00 00 00 C4 03", // Input 11
        "02 89 ${nodeAddr} 1B 83 00 00 1B 83 07 D3 00 00 00 00 C5 03"  // Input 12
    ]
    
    inputMuteSubscriptions.eachWithIndex { cmd, index ->
        def inputNum = index + 1
        if (logEnable) log.debug "Subscribing to input ${inputNum} mute: ${cmd}"
        sendHexCommand(cmd.replaceAll(" ", ""))
        pauseExecution(150)
        
    }
}

private subscribeToOutputMutes() {
    def nodeAddr = settings.nodeAddress ?: "DF47"
    
    // Output Mute Subscriptions (1-8)
    def outputMuteSubscriptions = [
        "02 89 ${nodeAddr} 1B 83 00 00 04 07 D0 00 00 00 00 C1 03", // Output 1
        "02 89 ${nodeAddr} 1B 83 00 00 04 07 D1 00 00 00 00 C0 03", // Output 2
        "02 89 ${nodeAddr} 1B 83 00 00 04 07 D2 00 00 00 00 C3 03", // Output 3
        "02 89 ${nodeAddr} 1B 83 00 00 04 07 D3 00 00 00 00 C2 03", // Output 4
        "02 89 ${nodeAddr} 1B 83 00 00 05 07 D0 00 00 00 00 C0 03", // Output 5
        "02 89 ${nodeAddr} 1B 83 00 00 05 07 D1 00 00 00 00 C1 03", // Output 6
        "02 89 ${nodeAddr} 1B 83 00 00 05 07 D2 00 00 00 00 C2 03", // Output 7
        "02 89 ${nodeAddr} 1B 83 00 00 05 07 D3 00 00 00 00 C3 03"  // Output 8
    ]
    
    outputMuteSubscriptions.eachWithIndex { cmd, index ->
        def outputNum = index + 1
        if (logEnable) log.debug "Subscribing to output ${outputNum} mute: ${cmd}"
        sendHexCommand(cmd.replaceAll(" ", ""))
        pauseExecution(150)

    }
}

private subscribeToInputVolumes() {
    def nodeAddr = settings.nodeAddress ?: "DF47"
    
    // Input Volume Subscriptions (1-12)
    def inputVolumeSubscriptions = [
        "02 8E ${nodeAddr} 1B 83 00 00 01 00 04 00 00 00 00 10 03", // Input 1
        "02 8E ${nodeAddr} 1B 83 00 00 01 00 0A 00 00 00 00 1E 03", // Input 2
        "02 8E ${nodeAddr} 1B 83 00 00 01 00 10 00 00 00 00 04 03", // Input 3
        "02 8E ${nodeAddr} 1B 83 00 00 01 00 16 00 00 00 00 1B 82 03", // Input 4
        "02 8E ${nodeAddr} 1B 83 00 00 1B 82 00 04 00 00 00 00 13 03", // Input 5
        "02 8E ${nodeAddr} 1B 83 00 00 1B 82 00 0A 00 00 00 00 1D 03", // Input 6
        "02 8E ${nodeAddr} 1B 83 00 00 1B 82 00 10 00 00 00 00 07 03", // Input 7
        "02 8E ${nodeAddr} 1B 83 00 00 1B 82 00 16 00 00 00 00 01 03", // Input 8
        "02 8E ${nodeAddr} 1B 83 00 00 1B 83 00 04 00 00 00 00 12 03", // Input 9
        "02 8E ${nodeAddr} 1B 83 00 00 1B 83 00 0A 00 00 00 00 1C 03", // Input 10
        "02 8E ${nodeAddr} 1B 83 00 00 1B 83 00 10 00 00 00 00 1B 86 03", // Input 11
        "02 8E ${nodeAddr} 1B 83 00 00 1B 83 00 16 00 00 00 00 00 03"  // Input 12
    ]
    
    inputVolumeSubscriptions.eachWithIndex { cmd, index ->
        def inputNum = index + 1
        if (logEnable) log.debug "Subscribing to input ${inputNum} volume: ${cmd}"
        sendHexCommand(cmd.replaceAll(" ", ""))
        pauseExecution(150)
    }
}

private getVolumeSubParam(inputNum) {
    // Returns the volume subscription parameter based on input number
    def subParams = [
        "00 04", "00 0A", "00 10", "00 16",  // Inputs 1-4
        "00 04", "00 0A", "00 10", "00 16",  // Inputs 5-8
        "00 04", "00 0A", "00 10", "00 16"   // Inputs 9-12
    ]
    return subParams[inputNum-1]
}

