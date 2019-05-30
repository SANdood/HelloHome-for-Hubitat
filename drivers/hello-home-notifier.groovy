/**
 *  Copyright 2019 Barry A. Burke
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
 *  Author: Barry A. Burke
 *
 * Revision History
 * 1.0.00	2019/05/30	Initial release
 */

def getVersionNum() { return "1.0.00" }
private def getVersionLabel() { return "Hello Home Notifier, version ${getVersionNum()}" }

metadata {
	definition (name: "Hello Home Notifier", namespace: "sandood", author:"Barry A. Burke (storageanarchy@gmail.com)",) {
	}
	capability	"Actuator"
	capability 	"Notification"
	capability	"Refresh"
	
	attribute 	'textMessage', 'string'
	attribute 	'myTile', 'string'
	
	command		'newMessage',['string']
	command		'helloHome',['string', 'string']		// device.helloHome(appName, message)


	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles(scale: 2) {
		valueTile("textMessage", "device.textMessage", height: 6, width: 5, decoration: "flat") {
			state "default",	label:'${currentValue}', defaultState: true, backgroundColor:"#ffffff"
		}

		main "textMessage"
		details "textMessage"
	}
	preferences {
		input "maxMessages", "number", defaultValue: 10
	}
}

// Parse incoming device messages to generate events
def parse(String description) {
	// None
}

void installed() {
	state.messages = []
	state.messageCount = 0
}

void updated() {
	refresh()
}

void initialize() {
}

def reset() {
	state.messages = []
	state.messageCount = 0
}

void refresh() {
	def theMessages = state.messages as List
	def theMsgsSize = theMessages.size()
	if (theMsgsSize != 0) displayMessages( theMessages, theMsgsSize - 1, settings.maxMessages.toInteger() )
}

def newMessage( String msg ){
	if ((msg != null) && (msg != '')) {
		Integer i = state.messageCount
		def theMessages = state.messages as List
		if ((i > 0) && (msg == theMessages[i-1].message)) {
			// Suppress identical messages, but update the time
			theMessages[i-1].epoch = now()
			state.messages = theMessages
			return
		}
		theMessages[i] = [epoch: now(), message: msg]
		displayMessages( theMessages, i, maxMessages.toInteger() )
		
		// OK, displayed, now let's trim the message queue if its too large
		def maxQueue = 50
		def k = 0
		if (i+1 > maxQueue) {
			def newMessages = []
			for (j=(i-maxQueue+1); j<=i; j++) {
				newMessages[k] = theMessages[j]
				k++
			}
			state.messages = newMessages
			state.messageCount = maxQueue
		} else {
			state.messages = theMessages
			state.messageCount = i+1
		}
	}
}

private String hhmm(time, fmt = 'h:mm a'){
    //def t = timeToday(time, location.timeZone)
    def f = new java.text.SimpleDateFormat(fmt)
    f.setTimeZone(location.timeZone ?: timeZone(time))
    return f.format(time)
}

private def displayMessages( List theMessages, Integer total, Integer count=5 ) {
	String msgs = ""
	String htmlMsgs = '<style>.hh{border-style:solid;background:#81BC00;border-radius:.4em;float:right;min-width:40%;max-width:100%;font-size:75%;text-align:left;line-height:110%;padding:5px;margin:0px;margin-left:15px;margin-right:15px;display:inline-block;}</style>' +
					  '<style>.ht{text-align:right;float:right;font-size:50%;margin:0px;min-width:100%;padding-right:22px;padding-bottom:5px;}</style><div>'
	boolean htmlDone = false
	Integer k = (count > total) ? total : count
	for (j=total; (j>=0 && (j>total-count)); j--) {
		String theMsg = theMessages[j].message
		String theTime = hhmm(theMessages[j].epoch).trim()
		msgs += theMsg + " (@ ${theTime})\n"
		def theMsgSize = theMsg.size()
		if (!htmlDone) {
			if ((18+18+theMsgSize+theTime.size()+6) <= 1024-(htmlMsgs.size())) {
				htmlMsgs += '<p class="hh">' + theMsg + '</p><p class="ht">' + theTime + '</p>'
			} else {
				htmlDone = true
			}
		}
	}
	htmlMsgs += '</div>'
	sendEvent(name: 'textMessage', value: msgs, isStateChange: true, displayed: false, descriptionText: "")
	sendEvent(name: 'myTile', value: htmlMsgs, isStateChange: true, displayed: false, descriptionText: "")
	log.info "(${htmlMsgs.size()}) " + theMessages[total].message
}
							 
def helloHome( String appName, String message ){
	if (appName == "") {
		log.error "helloHome() notifications require non-null appName (use ' ' instead)"
		log.info message + ' (helloHome)'
		return
	} else {
		sendLocationEvent(name: "HelloHome", descriptionText: message, value: appName, type: 'APP_NOTIFICATION')
	}
}

def deviceNotification(String text) {
	sendLocationEvent(name: "HelloHome", descriptionText: text, value: " ", type: 'DEVICE_NOTIFICATION')
}
