# Apple II Disk Browser
### Alternative
There is a beta release of [DiskBrowser2](https://github.com/dmolony/diskbrowser2) available.
### Features
- Cross-platform (Windows, MacOS, Linux)
- Disk formats
    - DOS
    - Prodos
    - Pascal
    - CPM
    - HDV
    - 2mg
    - shrinkIt, gzip, zip
    - hybrid disks
    - woz
    - Unidos
- File Formats
    - Applesoft and Integer Basic
    - Pascal
    - 6502
    - Appleworks
    - Visicalc
    - Graphics files 
- Disk Internals
    - Wizardry
    - Infocom
- Track / Sector / Block display
- Comprehensive disk listing

### Documentation
* [Usage](resources/usage.md)

### Installation
* Install the **latest** version of the [JDK](https://www.oracle.com/java/technologies/downloads/).
* Download DiskBrowser.jar from the [releases](https://github.com/dmolony/diskbrowser/releases) page.
* Double-click the jar file, or enter 'java -jar DiskBrowser.jar' in the terminal.
* Set your root folder (the top-level folder where you keep your disk images).

### Example Screens
#### Disk listing
Select a disk from the Disk Tree tab to display the catalog and disk layout.
![Disk listing](resources/disk1.png?raw=true "Disk listing")
#### Disk contents
Double-click a disk from the Disk Tree tab and a new tab appears. This tab contains a list of all the files on the disk. Click on a file and the Output panel will display the contents of that file. The disk sectors containing the file will also be highlighted in the Disk Layout panel.
![Disk catalog](resources/disk2.png?raw=true "Disk catalog")
#### Detailed sector display
Click on any sector in the Disk Layout panel and the Output panel will display the contents of that sector. If it is a recognised sector type, it will be formatted for easier reading.
![Sector details](resources/sector.png?raw=true "Sector details")
#### Cross-platform
Java runs on Windows, MacOS and Linux.
  
![Windows](resources/windows.png?raw=true "Windows")
#### Graphics 
![Graphics](resources/graphics.png?raw=true "Graphics")
#### Applesoft Formatting and Analysis
Applesoft programs are displayed in a modern, easily-readable format. A comprehensive cross-listing of variables, strings, calls and jumps is available. Easily find duplicate variable names.
For the truly retro look, programs can be displayed in the [original 40-column line wrap](resources/basic.md) mode.
![Applesoft](resources/basic.png?raw=true "Applesoft")
#### Pascal code
![Pascal](resources/pascal1.png?raw=true "Pascal text")
![Pascal](resources/pascal2.png?raw=true "Pascal internals")
#### Infocom
![Infocom](resources/zork.png?raw=true "Infocom")
#### Wizardry
Wizardry scenarios 1 to 3 are reasonably complete, Wizardry IV and V are partially done. For a dedicated Wizardry application see [WizardryApp](https://github.com/dmolony/WizardryApp).
![Wizardry](resources/wizardry.png?raw=true "Wizardry")
Scenarios 4 and 5 come on multiple disks, and they need to be named so that the only difference between disk names is the identifier before the '.dsk' suffix.
![Wizardry](resources/wizardry4.png?raw=true "Wizardry IV")
#### Visicalc
DiskBrowser has an inbuilt Visicalc processor which will evaluate the sheet and display the results.
![Visicalc](resources/visicalc.png?raw=true "Visicalc")
#### Complete Disk List
Generates a list of all the disks in your collection. The list can be sorted by any of the table headings. Checksums can be generated as needed, or for the whole collection.
![Disk List](resources/disklist.png?raw=true "Disk List")
Select a disk and click the duplicates button to see all of the disks that share the same checksum.
![Duplicates](resources/duplicates.png?raw=true "Duplicates")
