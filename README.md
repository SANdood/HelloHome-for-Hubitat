# Hello Home for Hubitat
An implementation of a Hello Home log display for Hubitat, with Notifications and Dashboard support.

### Features
* Parent/Child implementation
  * Hello Home Manager (parent) 
    * Subscribes to the Location Log and generates message texts for selected events that are sent to  the Hello Home Notifier (child) for display
    * Displays ALL Location Events generated with the `name: "HelloHome"` and `eventType: "APP_NOTIFICATION"` or `"DEVICE_NOTIFICATION"`. <br>Example:<br>
    `sendLocationEvent(name: "HelloHome", descriptionText: "Some message", value: app.label, type: 'APP_NOTIFICATION')`
    * Attempts to eliminate duplicate messages being sent back-to-back

  * Hello Home Notifier (child)
    * Receives messages from the Manager and generates HTML display content for the Hubitat Dashboard in the `myText` attribute (works best in a 2x2 Dashboard tile)
    * Implements the `Notifications` capability so other Hubitat apps can send their own Hello Home notifications - these will be posted with the `eventType: DEVICE_NOTIFICATION`
    * Hubitat apps can also send `APP_NOTIFICATION` events via the `device.helloHome(String appName, String message)` command entry point

### Installation
You need to install both the `Hello Home Manager` application and the `Hello Home Notifier` driver in their respective Code repositories on your Hubitat hub(s). For simplicity, here are the two links you'll need:
* **<> Apps Code**: https://raw.githubusercontent.com/SANdood/HelloHome-for-Hubitat/master/apps/hello-home-manager.groovy 
* **<> Drivers Code**: https://raw.githubusercontent.com/SANdood/HelloHome-for-Hubitat/master/drivers/hello-home-notifier.groovy

### Applications Support
At the moment, only my [Universal Ecobee Suite](https://github.com/SANdood/Ecobee-Suite) utilizes the notion of sending 'HelloHome' location events to create an activity log. If anyone else adopts this and wants me to list their Hubitat apps/drivers here, I'll be happy to create a list.

## Enjoy!
