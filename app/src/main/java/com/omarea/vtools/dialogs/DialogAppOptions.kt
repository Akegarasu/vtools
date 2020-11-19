package com.omarea.vtools.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.omarea.common.shared.FileWrite
import com.omarea.common.shared.MagiskExtend
import com.omarea.common.shell.AsynSuShellUnit
import com.omarea.common.shell.KeepShell
import com.omarea.common.shell.KeepShellPublic
import com.omarea.common.ui.DialogHelper
import com.omarea.model.Appinfo
import com.omarea.store.SpfConfig
import com.omarea.utils.CommonCmds
import com.omarea.vtools.R
import java.io.File
import java.util.*

/**
 * Created by helloklf on 2017/12/04.
 */

open class DialogAppOptions(protected final var context: Activity, protected var apps: ArrayList<Appinfo>, protected var handler: Handler) {
    private var allowPigz = false
    private var backupPath = CommonCmds.AbsBackUpDir
    private var userdataPath = ""

    init {
        userdataPath = context.filesDir.absolutePath
        userdataPath = userdataPath.substring(0, userdataPath.indexOf(context.packageName) - 1)
    }

    fun selectUserAppOptions() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_options_user, null)

        val dialog = DialogHelper.customDialogBlurBg(context, dialogView)
        dialogView.findViewById<View>(R.id.app_options_single_only).visibility = View.GONE
        dialogView.findViewById<View>(R.id.app_options_app_hide).setOnClickListener {
            dialog.dismiss()
            hideAll()
        }
        dialogView.findViewById<View>(R.id.app_options_clear).setOnClickListener {
            dialog.dismiss()
            clearAll()
        }
        dialogView.findViewById<View>(R.id.app_options_backup_apk).setOnClickListener {
            dialog.dismiss()
            backupAll(true, false)
        }
        dialogView.findViewById<View>(R.id.app_options_backup_all).setOnClickListener {
            dialog.dismiss()
            backupAll(true, true)
        }
        dialogView.findViewById<View>(R.id.app_options_uninstall).setOnClickListener {
            dialog.dismiss()
            uninstallAll()
        }
        /*
        dialogView.findViewById<View>(R.id.app_options_as_system).setOnClickListener {
            dialog.dismiss()
            moveToSystem()
        }
        */
        dialogView.findViewById<View>(R.id.app_options_dex2oat_speed).setOnClickListener {
            dialog.dismiss()
            buildAllSpeed()
        }
        dialogView.findViewById<View>(R.id.app_options_dex2oat_everything).setOnClickListener {
            dialog.dismiss()
            buildAllEverything()
        }
        dialogView.findViewById<TextView>(R.id.app_options_title).text = "请选择操作"

        // suspend
        dialogView.findViewById<View>(R.id.app_limit_p).visibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) View.VISIBLE else View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 暂停使用
            dialogView.findViewById<View>(R.id.app_limit_p_suspend).setOnClickListener {
                dialog.dismiss()
                suspendAll()
            }
            // 恢复使用
            dialogView.findViewById<View>(R.id.app_limit_p_unsuspend).setOnClickListener {
                dialog.dismiss()
                unsuspendAll()
            }
        }

        if (apps.any { it.enabled }) {
            dialogView.findViewById<View>(R.id.app_options_app_unfreeze).visibility = View.GONE
            dialogView.findViewById<View>(R.id.app_options_app_freeze).setOnClickListener {
                dialog.dismiss()
                disableAll()
            }
        } else {
            dialogView.findViewById<View>(R.id.app_options_app_freeze).visibility = View.GONE
            dialogView.findViewById<View>(R.id.app_options_app_unfreeze).setOnClickListener {
                dialog.dismiss()
                enableAll()
            }
        }
    }

    fun selectSystemAppOptions() {
        val dialogView = context.layoutInflater.inflate(R.layout.dialog_app_options_system, null)

        val dialog = DialogHelper.customDialogBlurBg(context, dialogView)
        dialogView.findViewById<View>(R.id.app_options_single_only).visibility = View.GONE
        dialogView.findViewById<View>(R.id.app_options_app_hide).setOnClickListener {
            dialog.dismiss()
            hideAll()
        }
        dialogView.findViewById<View>(R.id.app_options_clear).setOnClickListener {
            dialog.dismiss()
            clearAll()
        }
        dialogView.findViewById<View>(R.id.app_options_uninstall_user).setOnClickListener {
            dialog.dismiss()
            uninstallAllSystem(false)
        }
        dialogView.findViewById<View>(R.id.app_options_dex2oat_speed).setOnClickListener {
            dialog.dismiss()
            buildAllSpeed()
        }
        dialogView.findViewById<View>(R.id.app_options_dex2oat_everything).setOnClickListener {
            dialog.dismiss()
            buildAllEverything()
        }

        dialogView.findViewById<View>(R.id.app_options_delete).setOnClickListener {
            dialog.dismiss()
            deleteAll()
        }
        dialogView.findViewById<View>(R.id.app_options_uninstall).visibility = View.GONE

        dialogView.findViewById<TextView>(R.id.app_options_title).setText("请选择操作")

        dialogView.findViewById<View>(R.id.app_options_app_unfreeze).setOnClickListener {
            dialog.dismiss()
            enableAll()
        }
        dialogView.findViewById<View>(R.id.app_options_app_freeze).setOnClickListener {
            dialog.dismiss()
            disableAll()
        }

        // suspend
        dialogView.findViewById<View>(R.id.app_limit_p).visibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) View.VISIBLE else View.GONE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // 暂停使用
            dialogView.findViewById<View>(R.id.app_limit_p_suspend).setOnClickListener {
                dialog.dismiss()
                suspendAll()
            }
            // 恢复使用
            dialogView.findViewById<View>(R.id.app_limit_p_unsuspend).setOnClickListener {
                dialog.dismiss()
                unsuspendAll()
            }
        }
    }

    fun selectBackupOptions() {
        AlertDialog
                .Builder(context)
                .setTitle("请选择操作")
                .setCancelable(true)
                .setItems(arrayOf("删除备份", "还原", "还原(应用)", "还原(数据)")) { _, which ->
                    when (which) {
                        0 -> deleteBackupAll()
                        1 -> restoreAll(apk = true, data = true)
                        2 -> restoreAll(apk = true, data = false)
                        3 -> restoreAll(apk = false, data = true)
                    }
                }
                .show()
    }

    private fun checkRestoreData(): Boolean {
        val r = KeepShellPublic.doCmdSync("cd $userdataPath/${context.packageName}\necho `toybox ls -ld|cut -f3 -d ' '`\n echo `ls -ld|cut -f3 -d ' '`\n")
        return r != "error" && r.trim().isNotEmpty()
    }

    protected fun isMagisk(): Boolean {
        val keepShell = KeepShell(false)
        val result = keepShell.doCmdSync("su -v").toUpperCase(Locale.getDefault()).contains("MAGISKSU")
        keepShell.tryExit()
        return result
    }

    protected fun isTmpfs(dir: String): Boolean {
        val keepShell = KeepShell(false)
        val result = keepShell.doCmdSync("df | grep tmpfs | grep \"$dir\"").toUpperCase(Locale.getDefault()).trim().isNotEmpty()
        keepShell.tryExit()
        return result
    }

    protected fun execShell(sb: StringBuilder) {
        val layoutInflater = LayoutInflater.from(context)
        val dialog = layoutInflater.inflate(R.layout.dialog_loading, null)
        val textView = (dialog.findViewById(R.id.dialog_text) as TextView)
        textView.text = "正在获取权限"
        val alert = AlertDialog.Builder(context).setView(dialog).setCancelable(false).create()
        AsynSuShellUnit(ProgressHandler(dialog, alert, handler)).exec(sb.toString()).waitFor()
        DialogHelper.animDialog(alert)
    }

    open class ProgressHandler(dialog: View, protected var alert: AlertDialog, protected var handler: Handler) : Handler(Looper.getMainLooper()) {
        private var textView: TextView = (dialog.findViewById(R.id.dialog_text) as TextView)
        var progressBar: ProgressBar = (dialog.findViewById(R.id.dialog_app_details_progress) as ProgressBar)
        private var error = java.lang.StringBuilder()

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.obj != null) {
                if (msg.what == 0) {
                    textView.text = "正在执行操作..."
                } else if (msg.what == 5) {
                    error.append(msg.obj)
                    error.append("\n")
                } else if (msg.what == 10) {
                    if (msg.obj == true) {
                        textView.text = "操作完成！"
                    } else {
                        textView.text = "出现异常！"
                    }
                    handler.postDelayed({
                        alert.dismiss()
                        alert.hide()
                    }, 2000)
                } else {
                    val obj = msg.obj.toString()
                    if (obj.contains("[operation completed]")) {
                        progressBar.progress = 100
                        textView.text = "操作完成！"
                        handler.postDelayed({
                            try {
                                alert.dismiss()
                                alert.hide()
                            } catch (ex: Exception) {
                            }
                            if (error.isNotBlank()) {
                                DialogHelper.animDialog(AlertDialog.Builder(alert.context).setTitle("出现了一些错误").setMessage(error.toString()))
                            }
                        }, 1200)
                        handler.handleMessage(handler.obtainMessage(2))
                    } else if (Regex("^\\[.*]\$").matches(obj)) {
                        progressBar.progress = msg.what
                        val txt = obj
                                .replace("[copy ", "[复制 ")
                                .replace("[uninstall ", "[卸载 ")
                                .replace("[install ", "[安装 ")
                                .replace("[restore ", "[还原 ")
                                .replace("[backup ", "[备份 ")
                                .replace("[unhide ", "[显示 ")
                                .replace("[hide ", "[隐藏 ")
                                .replace("[delete ", "[删除 ")
                                .replace("[disable ", "[禁用 ")
                                .replace("[enable ", "[启用 ")
                                .replace("[trim caches ", "[清除缓存 ")
                                .replace("[clear ", "[清除数据 ")
                                .replace("[skip ", "[跳过 ")
                                .replace("[link ", "[链接 ")
                                .replace("[compile ", "[编译 ")
                        textView.text = txt
                    }
                }
            }
        }

        init {
            textView.text = "正在获取权限"
        }
    }

    protected fun confirm(title: String, msg: String, next: Runnable?) {
        DialogHelper.confirmBlur(context, title, msg, next)
    }

    /**
     * 检查是否可用pigz
     */
    protected fun checkPigz() {
        if (File("/system/xbin/pigz").exists() || File("/system/bin/pigz").exists()) {
            allowPigz = true
        }
    }

    /**
     * 备份选中的应用
     */
    protected fun backupAll(apk: Boolean = true, data: Boolean = true) {
        if (data) {
            if (!checkRestoreData()) {
                Toast.makeText(context, "抱歉，数据备份还原功能暂不支持你的设备！", Toast.LENGTH_LONG).show()
                return
            }
            confirm("备份应用和数据", "备份所选的${apps.size}个应用和数据？（很不推荐使用数据备份功能，因为经常会有兼容性问题，可能导致还原的软件出现FC并出现异常耗电）", Runnable {
                _backupAll(apk, data)
            })
        } else {
            _backupAll(apk, data)
        }
    }

    private fun _backupAll(apk: Boolean = true, data: Boolean = true) {
        checkPigz()

        val date = Date().time.toString()

        val sb = StringBuilder()
        sb.append("backup_date=\"$date\"\n")
        sb.append("\n")
        sb.append("backup_path=\"${CommonCmds.AbsBackUpDir}\"\n")
        sb.append("mkdir -p \${backup_path}\n")
        sb.append("\n")
        sb.append("\n")

        for (item in apps) {
            val packageName = item.packageName.toString()
            val path = item.path.toString()

            if (apk) {
                sb.append("rm -f \${backup_path}$packageName.apk\n")
                sb.append("\n")
                sb.append("echo '[copy $packageName.apk]'\n")
                sb.append("busybox cp -f $path \${backup_path}$packageName.apk\n")
                sb.append("\n")
            }
            if (data) {
                sb.append(
                        "killall -9 $packageName 2> /dev/null\n" +
                                "am kill-all $packageName 2> /dev/null\n" +
                                "am force-stop $packageName 2> /dev/null\n")
                sb.append("cd $userdataPath/$packageName\n")
                sb.append("echo '[backup ${item.appName}]'\n")
                if (allowPigz)
                    sb.append("busybox tar cpf - * --exclude ./cache --exclude ./lib | pigz > \${backup_path}$packageName.tar.gz\n")
                else
                    sb.append("busybox tar -czpf \${backup_path}$packageName.tar.gz * --exclude ./cache --exclude ./lib\n")
                sb.append("\n")
            }
        }
        sb.append("cd \${backup_path}\n")
        sb.append("chown sdcard_rw:sdcard_rw *\n")
        sb.append("chmod 777 *\n")
        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 还原选中的应用
     */
    protected fun restoreAll(apk: Boolean = true, data: Boolean = true) {
        if (data) {
            if (!checkRestoreData()) {
                Toast.makeText(context, "抱歉，数据备份还原功能暂不支持你的设备！", Toast.LENGTH_LONG).show()
                return
            }
            confirm("还原应用和数据",
                    "还原所选的${apps.size}个应用和数据？（很不推荐使用数据还原功能，因为经常会有兼容性问题，可能导致还原的软件出现FC并出现异常耗电）"
            ) {
                _restoreAll(apk, data)
            }
        } else {
            confirm("还原应用", "还原所选的${apps.size}个应用和数据？") {
                _restoreAll(apk, data)
            }
        }
    }

    private fun _restoreAll(apk: Boolean = true, data: Boolean = true) {
        val installApkTemp = FileWrite.getPrivateFilePath(context, "app_install_cache.apk")
        checkPigz()

        val sb = StringBuilder()
        sb.append("chown -R sdcard_rw:sdcard_rw \"$backupPath\" 2>/dev/null\n")
        sb.append("chmod -R 777 \"$backupPath\" 2>/dev/null\n")
        for (item in apps) {
            val packageName = item.packageName.toString()
            val apkPath = item.path.toString()
            if (apk && File("$backupPath$packageName.apk").exists()) {
                sb.append("echo '[install ${item.appName}]'\n")
                // sb.append("pm install -r $backupPath$packageName.apk\n")

                sb.append("rm -f $installApkTemp\n")
                sb.append("cp \"$backupPath$packageName.apk\" $installApkTemp\n")
                sb.append("pm install -r $installApkTemp 1> /dev/null\n")
                sb.append("rm -f $installApkTemp\n")
            } else if (apk && File(apkPath).exists()) {
                sb.append("echo '[install ${item.appName}]'\n")
                // sb.append("pm install -r \"$apkPath\" 1> /dev/null\n")

                sb.append("rm -f $installApkTemp\n")
                sb.append("cp \"$apkPath\" $installApkTemp\n")
                sb.append("pm install -r $installApkTemp 1> /dev/null\n")
                sb.append("rm -f $installApkTemp\n")
            }
            if (data && File("$backupPath$packageName.tar.gz").exists()) {
                sb.append("if [ -d $userdataPath/$packageName ]\n")
                sb.append(" then ")
                sb.append("echo '[restore ${item.appName}]'\n")
                //sb.append("pm clear $packageName\n")
                sb.append("sync\n")
                sb.append("cd $userdataPath/$packageName\n")
                sb.append("busybox tar -xzpf $backupPath$packageName.tar.gz\n")
                sb.append("install_group=`toybox ls -ld|cut -f3 -d ' '`\n")
                sb.append("install_own=`toybox ls -ld|cut -f4 -d ' '`\n")
                sb.append("for item in *\ndo\n")
                sb.append(
                        "if [[ ! \"\$item\" = \"lib\" ]] && [[ ! \"\$item\" = \"lib64\" ]]\n" +
                                "then\n" +
                                "chown -R \$install_group:\$install_own ./\$item\n" +
                                "fi\n" +
                                "done\n")
                //sb.append("chown -R --reference=$userdataPath/$packageName *\n")
                sb.append(" else ")
                sb.append("echo '[skip ${item.appName}]'\n")
                sb.append("sleep 1\n")
                sb.append("fi\n")
            }
        }
        sb.append("sync\n")
        sb.append("sleep 2\n")
        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 禁用所选的应用
     */
    protected fun disableAll() {
        confirm("冻结应用", "确定冻结选中的${apps.size}个应用？") {
            _disableAll()
        }
    }

    private fun _disableAll() {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[disable ${item.appName}]'\n")

            sb.append("pm disable ${packageName}\n")
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 启用所选的应用
     */
    protected fun enableAll() {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[enable ${item.appName}]'\n")

            sb.append("pm enable $packageName\n")
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 隐藏所选的应用
     */
    protected fun hideAll() {
        confirm("隐藏应用", "确定隐藏选中的${apps.size}个应用？") {
            _hideAll()
        }
    }

    /**
     * 暂停使用所选的应用
     */
    protected fun _suspendAll() {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[suspend ${item.appName}]'\n")

            sb.append("pm suspend $packageName\n")
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 暂停使用所选的应用
     */
    protected fun suspendAll() {
        confirm("暂停使用", "确定暂停使用选中的${apps.size}个应用？\n应用停用后，将在桌面上显示为灰色图标，需要恢复使用后才能打开", Runnable {
            _suspendAll()
        })
    }

    /**
     * 恢复使用所选的应用
     */
    protected fun unsuspendAll() {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[unsuspend ${item.appName}]'\n")

            sb.append("pm unsuspend $packageName\n")
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    @SuppressLint("ApplySharedPref")
    private fun _hideAll() {
        val spf = context.getSharedPreferences(SpfConfig.APP_HIDE_HISTORY_SPF, Context.MODE_PRIVATE).edit()
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[hide ${item.appName}]'\n")

            sb.append("pm hide $packageName\n")

            spf.putString(packageName, if (item.appName != null) item.appName as String? else packageName)
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
        spf.commit()
    }

    /**
     * 删除选中的应用
     */
    protected fun deleteAll() {
        confirm("删除应用", "已选择${apps.size}个应用，删除系统应用可能导致功能不正常，甚至无法开机，确定要继续删除？", Runnable {
            if (isMagisk() && !MagiskExtend.moduleInstalled() && (isTmpfs("/system/app") || isTmpfs("/system/priv-app"))) {
                DialogHelper.animDialog(AlertDialog.Builder(context)
                        .setTitle("Magisk 副作用警告")
                        .setMessage("检测到你正在使用Magisk作为ROOT权限管理器，并且/system/app和/system/priv-app目录已被某些模块修改，这可能导致这些目录被Magisk劫持并且无法写入！！")
                        .setPositiveButton(R.string.btn_confirm) { _, _ ->
                            _deleteAll()
                        })
            } else {
                _deleteAll()
            }
        })
    }

    private fun _deleteAll() {
        val sb = StringBuilder()
        sb.append(CommonCmds.MountSystemRW)
        var useMagisk = false
        for (item in apps) {
            val packageName = item.packageName.toString()
            // 先禁用再删除，避免老弹停止运行
            sb.append("echo '[disable ${item.appName}]'\n")
            sb.append("pm disable $packageName\n")

            sb.append("echo '[delete ${item.appName}]'\n")
            if (MagiskExtend.moduleInstalled()) {
                MagiskExtend.deleteSystemPath(item.path.toString())
                useMagisk = true
            } else {
                val dir = item.dir.toString()

                sb.append("rm -rf $dir/oat\n")
                sb.append("rm -rf $dir/lib\n")
                sb.append("rm -rf ${item.path}\n")
            }
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
        if (useMagisk) {
            DialogHelper.helpInfo(context, "已通过Magisk完成操作，请重启手机~", "")
        }
    }

    /**
     * 删除备份
     */
    protected fun deleteBackupAll() {
        confirm("删除备份", "永久删除这些备份文件？", Runnable {
            _deleteBackupAll()
        })
    }

    private fun _deleteBackupAll() {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[delete ${item.appName}]'\n")

            if (item.path != null) {
                sb.append("rm -rf ${item.path}\n")
                if (item.path == "$backupPath$packageName.apk") {
                    sb.append("rm -rf $backupPath$packageName.tar.gz\n")
                }
            } else {
                sb.append("rm -rf $backupPath$packageName.apk\n")
                sb.append("rm -rf $backupPath$packageName.tar.gz\n")
            }
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 清除数据
     */
    protected fun clearAll() {
        confirm("清空应用数据", "确定将选中的${apps.size}个应用数据清空？", Runnable {
            _clearAll()
        })
    }

    private fun _clearAll() {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[clear ${item.appName}]'\n")

            sb.append("pm clear $packageName\n")
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    /**
     * 卸载选中
     */
    protected fun uninstallAll() {
        val view = context.layoutInflater.inflate(R.layout.dialog_app_uninstall_mode, null)
        view.findViewById<TextView>(R.id.uninstall_info).text = "确定卸载选中的 ${apps.size} 个应用？"

        val dialog = DialogHelper.customDialogBlurBg(context, view)
        val userOnly = view.findViewById<CompoundButton>(R.id.uninstall_user_only)
        val keepData = view.findViewById<CompoundButton>(R.id.uninstall_keep_data)

        view.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()

            _uninstallAll(userOnly.isChecked, keepData.isChecked)
        }
    }

    /**
     * 卸载选中
     */
    protected fun uninstallAllSystem(updated: Boolean) {
        val view = context.layoutInflater.inflate(R.layout.dialog_app_uninstall_mode, null)
        view.findViewById<TextView>(R.id.uninstall_info).text = "确定卸载选中的 ${apps.size} 个系统应用？"

        val dialog = DialogHelper.customDialogBlurBg(context, view)
        val userOnly = view.findViewById<CompoundButton>(R.id.uninstall_user_only)
        val keepData = view.findViewById<CompoundButton>(R.id.uninstall_keep_data)

        userOnly.isEnabled = false
        if (updated) {
            userOnly.isEnabled = false
            keepData.isEnabled = false

            userOnly.isChecked = false
            keepData.isChecked = false
        } else {
            userOnly.isEnabled = false
            userOnly.isChecked = true
        }

        view.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }
        view.findViewById<View>(R.id.btn_confirm).setOnClickListener {
            dialog.dismiss()

            _uninstallAll(userOnly.isChecked, keepData.isChecked)
        }
    }

    private fun _uninstallAll(userOnly: Boolean, keepData: Boolean) {
        if (userOnly) {
            val um = context.getSystemService(Context.USER_SERVICE) as UserManager?
            val userHandle = android.os.Process.myUserHandle()
            if (um != null) {
                val uid = um.getSerialNumberForUser(userHandle)
                _uninstallAllOnlyUser(uid, keepData)
            } else {
                Toast.makeText(context, "获取用户ID失败！", Toast.LENGTH_SHORT).show()
            }
        } else {
            val sb = StringBuilder()

            for (item in apps) {
                val packageName = item.packageName.toString()
                sb.append("echo '[uninstall ${item.appName}]'\n")

                if (keepData) {
                    sb.append("pm uninstall -k $packageName\n")
                } else {
                    sb.append("pm uninstall $packageName\n")
                }
            }

            sb.append("echo '[operation completed]'\n")
            execShell(sb)
        }
    }

    private fun _uninstallAllOnlyUser(uid: Long, keepData: Boolean) {
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[uninstall ${item.appName}]'\n")

            if (keepData) {
                sb.append("pm uninstall -k --user $uid $packageName\n")
            } else {
                sb.append("pm uninstall --user $uid $packageName\n")
            }
        }

        sb.append("echo '[operation completed]'\n")
        execShell(sb)
    }

    protected fun buildAllSpeed() {
        buildAll("speed")
    }

    protected fun buildAllEverything() {
        buildAll("everything")
    }

    private fun buildAll(mode: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(context, "该功能只支持Android N（7.0）以上的系统！", Toast.LENGTH_SHORT).show()
            return
        }
        val sb = StringBuilder()
        for (item in apps) {
            val packageName = item.packageName.toString()
            sb.append("echo '[compile ${item.appName}]'\n")

            sb.append("cmd package compile -m $mode $packageName\n\n")
        }

        sb.append("echo '[operation completed]'\n\n")
        execShell(sb)
    }
}
