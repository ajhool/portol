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

public class FFMPEGController {

	public static final String mffmpegBin = "ffmpeg";

	public class Argument
	{
		String key;
		String value;

		public static final String VIDEOCODEC = "-vcodec";
		public static final String AUDIOCODEC = "-acodec";

		public static final String VIDEOBITSTREAMFILTER = "-vbsf";
		public static final String AUDIOBITSTREAMFILTER = "-absf";

		public static final String VERBOSITY = "-v";
		public static final String FILE_INPUT = "-i";
		public static final String ENABLE_REALTIME = "-re";
		public static final String KEYFRAME_INTERVAL = "-g";
		public static final String AUDIO_BITRATE = "-ab";
		public static final String AUDIO_PROFILE = "-profile:a";
		public static final String FRAGMENT_SIZE = "-frag_size";
		public static final String FRAGMENT_DUR = "-frag_duration";
		public static final String DISABLE_VIDEO = "-vn";
		public static final String VIDEO_BITRATE = "-vb";
		public static final String MOVFLAGS = "-movflags";
		public static final String DISABLE_AUDIO = "-an";
		public static final String ENABLE_OUTFILE_OVERWRITE = "-y";
		public static final String SIZE = "-s";
		public static final String FRAMERATE = "-r";
		public static final String FORMAT = "-f";
		public static final String BITRATE_VIDEO = "-b:v";

		public static final String BITRATE_AUDIO = "-b:a";
		public static final String CHANNELS_AUDIO = "-ac";
		public static final String FREQ_AUDIO = "-ar";

		public static final String STARTTIME = "-ss";
		public static final String DURATION = "-t";


	}

	public ArrayList<String> generateFFMPEGCommand(Content toTranscode){
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mffmpegBin);

		//add streaming input
		cmd.add(Argument.FILE_INPUT);

		cmd.add("REDACTED");


		cmd.add("-c:v");
		cmd.add("copy");

		cmd.add(Argument.DISABLE_AUDIO);

		cmd.add(Argument.FORMAT);
		cmd.add("mpegts");

		cmd.add(Argument.MOVFLAGS);
		cmd.add("frag_keyframe+empty_moov");


		//add audio output file
		cmd.add("udp://127.0.0.1:5001");


		cmd.add(Argument.DISABLE_VIDEO);
		cmd.add("-c:a");
		cmd.add("copy");

		cmd.add(Argument.FORMAT);
		cmd.add("mpegts");

		cmd.add(Argument.MOVFLAGS);
		cmd.add("frag_keyframe+empty_moov");

		//add audio output file
		cmd.add("udp://127.0.0.1:5000");

		return cmd;

	}

	public int execProcess(List<String> cmds, ShellCallback sc, File fileExec) throws IOException, InterruptedException {		

		//ensure that the arguments are in the correct Locale format
		for (String cmd :cmds)
		{
			cmd = String.format(Locale.US, "%s", cmd);
		}

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileExec);

		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds)
		{
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}

		sc.shellOut(cmdlog.toString());

		Process process = pb.start();    

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "ERROR", sc);

		// any output?
		StreamGobbler outputGobbler = new 
				StreamGobbler(process.getInputStream(), "OUTPUT", sc);

		errorGobbler.start();
		outputGobbler.start();

		int exitVal = process.waitFor();

		sc.processComplete(exitVal);

		return exitVal;

	}


	private class StreamGobbler extends Thread
	{
		InputStream is;
		String type;
		ShellCallback sc;

		StreamGobbler(InputStream is, String type, ShellCallback sc)
		{
			this.is = is;
			this.type = type;
			this.sc = sc;
		}

		public void run()
		{
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null)
					if (sc != null)
						sc.shellOut(line);

			} catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}
	}


	public static int killVideoProcessor (boolean asRoot, boolean waitFor) throws IOException
	{
		int killDelayMs = 300;

		int result = -1;

		int procId = -1;

		while ((procId = ShellUtils.findProcessId(mffmpegBin)) != -1)
		{

			String[] cmd = { ShellUtils.SHELL_CMD_KILL + ' ' + procId + "" };

			try { 
				result = ShellUtils.doShellCommand(cmd, new StdoutCallback(), asRoot, waitFor);
				Thread.sleep(killDelayMs); }
			catch (Exception e){}
		}

		return result;
	}


	public static void main(String[] args) throws IOException, InterruptedException{

		if(args.length > 0 && args[0].equalsIgnoreCase("kill")){
			killVideoProcessor(false, true);
			return;
		}

		ShellCallback sc = new StdoutCallback();
		FFMPEGController me = new FFMPEGController();


		List<String> cmds = me.generateFFMPEGCommand(null);

		int exit = me.execProcess(cmds, sc, null);
		System.out.println("goodbye. Exit code: " + exit);
	}


}
