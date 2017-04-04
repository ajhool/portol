package ffmpeg;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.portol.common.model.content.Content;

public class eDashController {

	public static final String packagerBin = "../../bin/packager";

	public enum StreamType {
		VIDEO, AUDIO
	}

	public ArrayList<String> generateEDASHCommand(Content toTranscode, String repId, StreamType type) throws Exception {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(packagerBin);

		switch (type) {
		case AUDIO:
			cmd.add("input=udp://127.0.0.1:5000,stream=audio,init_segment=test_audio_init.mp4,segment_template=test_audio-$Time$.mp4,bandwidth=130000");
			break;
		case VIDEO:
			cmd.add("input=udp://127.0.0.1:5001,stream=video,init_segment=test_video_init.mp4,segment_template=test_video-$Time$.mp4,bandwidth=2000000");
			break;
		default:
			throw new Exception("unrecognized media DASHing type");
		}

		cmd.add("--profile");
		cmd.add("live");
		cmd.add("--mpd_output");
		cmd.add("live.mpd");

		return cmd;

	}

	public int execProcess(List<String> cmds, ShellCallback sc, File fileExec)
			throws IOException, InterruptedException {

		// ensure that the arguments are in the correct Locale format
		for (String cmd : cmds) {
			cmd = String.format(Locale.US, "%s", cmd);
		}

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileExec);

		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds) {
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}

		sc.shellOut(cmdlog.toString());

		Process process = pb.start();

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", sc);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", sc);

		errorGobbler.start();
		outputGobbler.start();

		int exitVal = process.waitFor();

		sc.processComplete(exitVal);

		return exitVal;

	}

	private class StreamGobbler extends Thread {
		InputStream is;
		String type;
		ShellCallback sc;

		StreamGobbler(InputStream is, String type, ShellCallback sc) {
			this.is = is;
			this.type = type;
			this.sc = sc;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					if (sc != null)
						sc.shellOut(line);

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static int killVideoProcessor(boolean asRoot, boolean waitFor) throws IOException {
		int killDelayMs = 300;

		int result = -1;

		int procId = -1;

		while ((procId = ShellUtils.findProcessId(packagerBin)) != -1) {

			String[] cmd = { ShellUtils.SHELL_CMD_KILL + ' ' + procId + "" };

			try {
				result = ShellUtils.doShellCommand(cmd, new StdoutCallback(), asRoot, waitFor);
				Thread.sleep(killDelayMs);
			} catch (Exception e) {
			}
		}

		return result;
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		if (args.length > 0 && args[0].equalsIgnoreCase("kill")) {
			killVideoProcessor(false, true);
			return;
		}

		EDASHCallback sc = new EDASHCallback();
		eDashController me = new eDashController();

		// List<String> cmds = me.generateEDASHCommand(null);

		// int exit = me.execProcess(cmds, sc, null);
		// System.out.println("goodbye. Exit code: " + exit);
	}

}
