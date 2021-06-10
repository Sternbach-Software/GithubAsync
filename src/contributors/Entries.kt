package shmuly.networking

import kotlinx.coroutines.*
import java.awt.AWTException
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.TrayIcon
import java.awt.event.ActionListener
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.JOptionPane
import javax.swing.UIManager
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess
var localCommits = mutableListOf<String>()
const val token = "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"

interface Entries: CoroutineScope {

    val job: Job

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    /**
     * I think this function mutates the list called [localCommits] if there is a new local commit
     * @returns [localCommits]
     * */
    private fun updateLocalCommits(directory:String = "C:\\Users\\shmue\\OneDrive\\Documents\\GitHub\\TherapyToolkitDatabase"/*System.getProperty("user.dir")*/): MutableList<String> {
        val builder = ProcessBuilder(
            "cmd.exe", "/c", "cd $directory && git log --name-status"
        )
        builder.redirectErrorStream(true)
        val p = builder.start()
        val r = BufferedReader(InputStreamReader(p.inputStream))
        var line: String?

        while (true) {
            line = r.readLine()
            if (line == null) {
                break
            }
            if(line.length == 47 && line.startsWith("commit")) {
                val element = line.substringAfter("commit ")
                if (localCommits.isNotEmpty() && !localCommits.contains(element)) {
                    println("Commits up to date")
                    continue
                } else localCommits.add(element)
            }
        }
        return localCommits
    }

    suspend fun checkForUpdate(service: GitHubService){
        val cloudCommit = getLatestCloudCommit(service)
        if(cloudCommit !in updateLocalCommits()){
            println("cloud commit not in local, cloudCommit=$cloudCommit, localCommits=$localCommits")
            displayTray()
        }
        else println("Local up-to-date")
    }

    suspend fun getLatestCloudCommit(service: GitHubService): String {
        return service
            .getLatestCommit()
            .body()!!
            .sha
    }

    @Throws(AWTException::class)
    fun displayTray() {
        //Obtain only one instance of the SystemTray object
        val tray = SystemTray.getSystemTray()

        //If the icon is a file
        val image = Toolkit.getDefaultToolkit().createImage("C:\\Users\\shmue\\Downloads\\diagnosis predictor.svg")
        //Alternative (if the icon is on the classpath):
        //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));
        val trayIcon = TrayIcon(image, "Therapy Toolkit Database Creation Tool")
        //Let the system resize the image if needed
        trayIcon.isImageAutoSize = true
        //Set tooltip text for the tray icon
        trayIcon.toolTip = "Therapy Toolkit Database Creation Tool"
        tray.add(trayIcon)
        trayIcon.displayMessage("Pull new commit", "A new commit is available in the cloud to pull", TrayIcon.MessageType.INFO)

    }
    fun init() {
        // Start a new loading on 'load' click
        addLoadListener {
            saveParams()
            fun textboxMessage(message: String, title: String?, messageType: Int = JOptionPane.ERROR_MESSAGE) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    messageType
                )
            }
            if (SystemTray.isSupported()) {
                loopForCommits()
            }
            else textboxMessage("System notification not supported!", "Notification not supported", JOptionPane.ERROR_MESSAGE)
        }
        // Save preferences and exit on closing the window
        addOnWindowClosingListener {
            job.cancel()
            saveParams()
            exitProcess(0)
        }

        // Load stored interval
        loadInitialParams()
    }

    fun loopForCommits() {
        val interval = getInterval().toLong()
        val service = createGitHubService()
        val startTime = System.nanoTime().apply{println("Time originally gotten")}
        launch(Dispatchers.Default) {
            while(true){
                checkForUpdate(service).apply{ println("Check for updates ran, seconds: ${(System.nanoTime()-startTime)/1_000_000_000}")}
                Thread.sleep(interval)
            }
        }.setUpCancellation()
    }

    private fun Job.setUpCancellation() {
        // make active the 'cancel' button
        setActionsStatus(newLoadingEnabled = false, cancellationEnabled = true)

        val loadingJob = this

        // cancel the loading job if the 'cancel' button was clicked
        val listener = ActionListener {
            loadingJob.cancel()
            setActionsStatus(true,false)
        }
        addCancelListener(listener)

        // update the status and remove the listener after the loading job is completed
        launch {
            loadingJob.join()
            setActionsStatus(newLoadingEnabled = true, false)
            removeCancelListener(listener)
        }
    }

    fun loadInitialParams() {
        setInterval(loadStoredParams())
    }

    fun saveParams() {
        val interval = getInterval()
        if (interval == 0) removeStoredParams()
        else saveParams(interval)
    }

    fun setActionsStatus(newLoadingEnabled: Boolean, cancellationEnabled: Boolean = false)

    fun addCancelListener(listener: ActionListener)

    fun removeCancelListener(listener: ActionListener)

    fun addLoadListener(listener: () -> Unit)

    fun addOnWindowClosingListener(listener: () -> Unit)

    fun setInterval(interval: Int)

    fun getInterval(): Int
}