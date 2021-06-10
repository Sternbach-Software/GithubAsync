//package shmuly.notification
//
//import java.awt.AWTException
//import java.awt.SystemTray
//import java.awt.Toolkit
//import java.awt.TrayIcon
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.net.URL
//import javax.swing.JOptionPane
//
//
//class TrayIconDemo {
//    var localCommits = mutableListOf<String>()
//
//    private fun updateLocalCommits(directory:String = "\"C:\\Users\\shmue\\OneDrive\\Documents\\GitHub\\TherapyToolkitDatabase\"") {
//        val builder = ProcessBuilder(
//            "cmd.exe", "/c", "cd $directory && git log --name-status"
//        )
//        builder.redirectErrorStream(true)
//        val p = builder.start()
//        val r = BufferedReader(InputStreamReader(p.inputStream))
//        var line: String?
//
//        while (true) {
//            line = r.readLine()
//            if (line == null) {
//                break
//            }
//            if(line.length == 47 && line.startsWith("commit")) {
//                val element = line.substringAfter("commit ")
//                if (localCommits.isNotEmpty() && localCommits.last() == element) {
//                    println("Commits up to date")
//                    break
//                } else localCommits.add(element)
//            }
//        }
//    }
//    @Throws(AWTException::class)
//    fun displayTray() {
//        //Obtain only one instance of the SystemTray object
//        val tray = SystemTray.getSystemTray()
//
//        //If the icon is a file
//        val image = Toolkit.getDefaultToolkit().createImage("C:\\Users\\shmue\\Downloads\\diagnosis predictor.svg")
//        //Alternative (if the icon is on the classpath):
//        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));
//        val trayIcon = TrayIcon(image, "Therapy Toolkit Database Creation Tool")
//        //Let the system resize the image if needed
//        trayIcon.isImageAutoSize = true
//        //Set tooltip text for the tray icon
//        trayIcon.toolTip = "Therapy Toolkit Database Creation Tool"
//        tray.add(trayIcon)
//        trayIcon.displayMessage("Pull new commit", "A new commit is available in the cloud to pull", TrayIcon.MessageType.INFO)
//        fun start() {
//            if (SystemTray.isSupported()) {
//                val td = TrayIconDemo()
//                td.loopForUpdates()
//            } else {
//                ConditionMakerFunctionLibrary.textboxMessage("System notification not supported!", "Notification not supported", JOptionPane.ERROR_MESSAGE)
//            }
//        }
//    }
//
//}