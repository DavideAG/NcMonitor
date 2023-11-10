## 1.0.0 (November, 10, 2023)

- add cpu core spinner support

## 0.6.0 (November, 7, 2023)

- update to JDK 17 and Gradle 7.5
- fix bug about free disk space

## 0.5.4 (April, 27, 2022)

- added a method to show free disk space in a more human friendly way

## 0.5.3 (April, 27, 2021)

- Defined layout for settings page
- created author ref to http://giorgiodavide.it with "Open on Phone" animation
- added spinner for CPUs in order to compute the CPU usage in the correct way (default disabled)
- added app version information in the setting page

## 0.5.2 (July, 14, 2020)

- Calculating the disk free memory in the correct way if it is less than 1 GB
- Fixed a potential cast issue on cpuLoad

## 0.5.0 (July, 13, 2020)

- Implemented pull down to refresh that can be used to update server metrics

## 0.4.0 (July, 9, 2020)

- changed loading screen
- now user errors are handled correctly
- added sliding server domain name in the result page
- fixed network connectivity check

## 0.3.0 (July, 8, 2020)

- radio buttons to support http and https
- dotted password field
- check on internet connectivity
- added header copyright

## 0.2.0 (July, 5, 2020)

- added login activity
- added login layout
- progress bar during the execution of the GET
- added NcMonitor app icon
- moved the login and networking logic to the login activity

## 0.1.0 (July, 3, 2020)

- ambient display support
- horizontal layout for error message
- added OkHttpRequest class
- new layout
- beta implementation working for: cpuLoad, ram, swap, disk
