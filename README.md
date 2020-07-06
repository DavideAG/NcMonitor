# NcMonitor - Nextcloud monitoring WearOS app :watch:
NcMonitor is a native [WearOS](https://wearos.google.com/#hands-free-help) application written in [Kotlin](https://kotlinlang.org/) that can be used to check the state of your [Nextcloud](https://nextcloud.com/) server. It is simple, fast and light. You have just to look up at your wrist to know if your NC instance is working properly or not.

<br>
<p align="center">
<img src="./images/login.png" 
      alt="NcMonitor login " 
      height="330" />
</p>

<p align="center">
<img src="./images/status.png" 
      alt="NcMonitor status " 
      height="325" />
</p>

## Displayed metrics
Currently this application is still under development and the information that are displayed are:

- CPU usage (last-minute)
- RAM usage
- SWAP usage
- Disk free space

The final goal is to provide to the user more information about the Nc instance. In the future more metrics will be added.

Data are retrived from [Nextcloud/serverinfo](https://github.com/nextcloud/serverinfo).

## How to use it
To use the application you have to install the application (see Project status) and then you have to log-in in your Nc server using:

- select your HyperText Transfer Protocol (`http` or `https`)
- server URL (e.g. myserver.com)
- Nextcloud username
- Nextcloud password

## Changelog
Changelog information are reported [here](https://github.com/DavideAG/NcMonitor/blob/master/CHANGELOG.md).

## Project status
This application is in `beta` phase and is working but it is not yet released in Google Play Store.
If you want to use this application you can download the `apk` in the release section of this repository or you can manually build the project.

## License
This application is licensed under the `AGPLv3`

## Contact
Any comment or idea about the project is appreciated. You can ping me at [davide@giorgiodavide.it](mailto:davide@giorgiodavide.it?subject=[GitHub]%20NcMonitor)
