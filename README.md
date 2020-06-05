# JioFi Status

Get Jio-Fi battery status and shows a non dismissable notification which shows Jio-Fi battery status
Notification is displayed via service.

### How it works
Jio-Fi router page shows info about device info like battery status, signal strength, downlink & uplink speed, 
App gets those data using Jsoup and display in app, App fetches data after every 10 secs (Notification too).
