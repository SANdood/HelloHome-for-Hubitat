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
 *
 *  Author: Barry A. Burke
 *
 * Revision History
 * 1.0.00	2019/05/30	Initial release
 */
import java.util.TimeZone
def getVersionNum() { return "1.0.00" }
private def getVersionLabel() { return "Hello Home for Hubitat, version ${getVersionNum()}" }

definition(
    name: "Hello Home Manager",
    namespace: "sandood",
	author: "Barry A. Burke (storageanarchy@gmail.com)",
    description: "Parent app to monitor and report Location Events (like Hello, Home on ST).",
    category: "My Apps",
    iconUrl: "https://raw.githubusercontent.com/SANdood/Icons/master/Hubitat/HelloHomeHubitat1x.png",
    iconX2Url: "https://raw.githubusercontent.com/SANdood/Icons/master/Hubitat/HelloHomeHubitat2x.png"
)

preferences {
	section("Hello Home Configuration") {
		input "theEvents", "enum", multiple: true, title: "Monitor these events", options: ['HelloHome', 'mode', 'sunrise', 'sunset', 'systemStart'], required: true
		input "shortMsg", "bool", title: "Use short initialization notice?", defaultValue: false
	}
}

def installed()
{
	initialize()
}

def updated()
{
	unsubscribe()
	initialize()
}

def uninstalled() {
	deleteChildDevice(state.displayerDNI)
}

def initialize() {
	if (state.displayerDNI == null) {
		def child = addChildDevice('sandood', "Hello Home Notifier", "helloHome-${app.id}", location.hubs[0]?.id, ["label":"Hello Home Notifier", completedSetup:true])
		state.displayerDNI = "helloHome-${app.id}"
	}
	//subscribe(location, "HelloHome", locationEventHandler)
	//settings.theEvents.each {
	//	subscribe(location, it, locationEventHandler)
	//}
	subscribe(location, locationEventHandler)
	
	if (!settings.shortMsg) {
		String locName = location.name ? "This location is named ${location.name}. " : 'Please set the Location name in Settings/Location. '
		String zipCode = location.zipCode ? "The Postal Code is ${location.zipCode}. " : 'Please set the Postal Code in Settings/Location. '
		String gpsCode = (location.latitude && location.longitude) ? "The Geographic Coordinates are ${location.latitude}/${location.longitude}. " : "Please specify the Geographic Coordinates in Settings/Location. "
		String timeZone
		if (location.timeZone) {
			TimeZone tz = location.timeZone
			boolean inDST = (tz.observesDaylightTime() && tz.inDaylightTime(new Date()))
			// tzName = inDST ? tz.displayName() : tz.getDisplayName().replace('Standard','Daylight Savings')
			def tzName = tz.getDisplayName(inDST, 1)
			timeZone = "The Time Zone is ${tzName}. "
		} else {
			timeZone =  "Please select the Time Zone in Settings/Location. "
		}
		String tempScal= location.temperatureScale ? "Temperatures will be displayed in ${location.temperatureScale == 'F' ? 'Fahrenheit' : 'Celsius'} degrees." : "Please select the temperature scale in Settings/Location."
		sendLocationEvent(name: "HelloHome", descriptionText: locName+zipCode+gpsCode+timeZone+tempScal, value: app.label, type: 'APP_NOTIFICATION')
	} else {
		sendLocationEvent(name: "HelloHome", descriptionText: "Hello Home for Hubitat initialized", value: app.label, type: 'APP_NOTIFICATION')
	}
}

def locationEventHandler(evt) {
	log.trace "Location Event ${evt.name} from ${evt.value}"
	def msgText = ""
	switch(evt.name) {
		case 'HelloHome':
			msgText = evt.descriptionText
			// Somebody sent a HelloHome with the wrong type
			if ((evt.type != "APP_NOTIFICATION") && (evt.type != "DEVICE_NOTIFICATION")) log.warn "Invalid Type. Name: ${evt.name}, Text: ${evt.descriptionText}, Value: ${evt.value}, Type: ${evt.type}"
			break;
		case 'systemStart':
		case 'mode':
		case 'id':
		case 'name':
		case 'temperatureScale':
		case 'timeZone':
		case 'zipCode':
		case 'latitude':
		case 'longitude':
			msgText = evt.descriptionText
			break;
		case 'sunrise':
			msgText = "It's Sunrise at ${location.name}"
			break;
		case 'sunset':
			msgText = "It's Sunset at ${location.name}"
			break;
		case 'deviceJoin':
		case 'AskAlexaMQRefresh':
		case 'sunsetTime':
		case 'sunriseTime':
		case 'ssdpTerm':
			// Just ignore these
			break;
		default:
			log.warn "Unknown Event Name. Name: ${evt.name}, Text: ${evt.descriptionText}, Value: ${evt.value}, Type: ${evt.type}"
			break;
	}
	if (msgText != "") {
		log.info msgText + " (${evt.name})"
		def child = getChildDevice(state.displayerDNI)
		child.newMessage( msgText )
	}
}
