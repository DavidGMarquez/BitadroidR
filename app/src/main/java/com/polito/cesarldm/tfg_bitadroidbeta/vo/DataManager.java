package com.polito.cesarldm.tfg_bitadroidbeta.vo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.polito.cesarldm.tfg_bitadroidbeta.R;

import info.plux.pluxapi.bitalino.BITalinoFrame;


/**
 * Saves and compresses recording data into android' external file system
 * @author Carlos Marten, modified partly by César López de Mata, September-2017
 *
 */
public class DataManager {
	
	// Standard debug constant
	private static final String TAG = DataManager.class.getName();
	
	public static final int MSG_PERCENTAGE = 99;
	
	public static final int STATE_APPENDING_HEADER = 1;
	public static final int STATE_COMPRESSING_FILE = 2;
	
	private Messenger client = null;

	private ChannelConfiguration configuration;
	private OutputStreamWriter outStreamWriter,outStreamWriterBPMRR,outStreamWriterLocation;
	private BufferedWriter bufferedWriter,bufferedWriterBPMRR,bufferedWriterLocation;
	private int BUFFER = 524288; // 0.5MB Optimal for Android devices
	private int numberOfChannelsActivated;
    private String tempfilepath=Environment.getExternalStorageDirectory().toString()+ Constants.TEMP_APP_DIRECTORY;
    private boolean pulseMetricsON=false;
	private String recordingName;
	private String duration;
	private BluetoothDevice device;
	Location lastLocation;
	
	
	private Context context;
	
	/**
	 * Constructor. Initializes the number of channels activated, the outStream
	 * write and the Buffered writer
	 */
	public DataManager(Context serviceContext, String _recordingName, ChannelConfiguration _configuration, BluetoothDevice device) {
		this.context = serviceContext;
		this.recordingName = _recordingName;
		this.configuration = _configuration;
		this.device=device;
		this.numberOfChannelsActivated = configuration.getActiveChannelListSize();
        if(createPath()) {
            initializeFrameFile();
            initializeBPMRRFile();
            initializeLocationFile();
        }
	}
    private boolean createPath(){
        File directory =new File(tempfilepath);
        if(!directory.exists()){
            if(directory.mkdirs()){
                Log.d(TAG,"directory created");
                return true;
            }else{
                return  false;
            }
        }else{
            return true;
        }
    }
	private void initializeFrameFile(){
        File frameTempFile=new File(tempfilepath+Constants.TEMP_FILE);
		try {
			outStreamWriter = new OutputStreamWriter(context.openFileOutput(Constants.TEMP_FILE, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write frames on, not found", e);
		}
		bufferedWriter = new BufferedWriter(outStreamWriter);
	}
	// New method added by César López de Mata. Same functionality as previous "initializeFrameFile()"
	private void initializeBPMRRFile(){
		try {
			outStreamWriterBPMRR = new OutputStreamWriter(context.openFileOutput(Constants.TEMP_BPMRR_FILE, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write BPMRR on, not found", e);
		}
		bufferedWriterBPMRR = new BufferedWriter(outStreamWriterBPMRR);
	}
    // New method added by César López de Mata. Same functionality as previous "initializeFrameFile()"
	private void initializeLocationFile(){
		try {
			outStreamWriterLocation = new OutputStreamWriter(context.openFileOutput(Constants.TEMP_LOC_FILE, Context.MODE_PRIVATE));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "file to write location on, not found", e);
		}
		bufferedWriterLocation = new BufferedWriter(outStreamWriterLocation);
	}

	
	/**
	 * Writes a frame (row) on text file that will go after the header. Returns
	 * true if wrote successfully and false otherwise.
	 */
	private final StringBuilder sb = new StringBuilder(400);
	public boolean writeFrameToTmpFile(BITalinoFrame frame, int frameSeq) {
		sb.delete(0, sb.length());
		try {
			sb.append(frameSeq).append("\t");
            sb.append(frame.getDigital(0)+"\t");
			sb.append(frame.getDigital(1)+"\t");
			sb.append(frame.getDigital(2)+"\t");
			sb.append(frame.getDigital(3)+"\t");
            for(int i=0; i<configuration.activeChannels.length;i++){
				sb.append(frame.getAnalog(configuration.activeChannels[i])+"\t");
			}
			bufferedWriter.write(sb.append("\n").toString());
		} catch (Exception e) {
			try {bufferedWriter.close();} catch (Exception e1) {}
			Log.e(TAG, "Exception while writing frame row", e);
			return false;
		}
		return true;
	}
   // New method added by César López de Mata. Same functionality as previous "writeFrameToTmpFile()"
	private final StringBuilder sbloc = new StringBuilder(400);
	public boolean writeLocationToTmpFile(Location location) {
		float distance;
		sbloc.delete(0, sbloc.length());
		if (lastLocation != null) {
			distance = location.distanceTo(lastLocation);
		} else {distance = 0;
				}
		try {
			sbloc.append(location.getLatitude()+"\t");
			sbloc.append(location.getLongitude()+"\t");
			sbloc.append(location.getAltitude()+"\t");
			sbloc.append(distance+"\t");
			bufferedWriterLocation.write(sbloc.append("\n").toString());
		} catch (Exception e) {
			try {bufferedWriterLocation.close();} catch (Exception e1) {}
			Log.e(TAG, "Exception while writing location row", e);
			return false;
		}
        Log.e(TAG, "LOCATION WELL WRITTEN");
        Log.e(TAG, "LOCATION written in:"+bufferedWriterLocation.toString());
		lastLocation=location;
		return true;
	}
    // New method added by César López de Mata. Same functionality as previous "writeFrameToTmpFile()"
    private final StringBuilder sbbpm = new StringBuilder(400);
    public boolean writeBPMToTmpFile(float temprr,float tempbpm) {
        if(!pulseMetricsON) {
            pulseMetricsON = true;
        }
        sbbpm.delete(0, sbbpm.length());
        try {
            sbbpm.append(temprr+"\t");
            sbbpm.append(tempbpm+"\t");
            bufferedWriterBPMRR.write(sbbpm.append("\n").toString());
        } catch (Exception e) {
            try {bufferedWriterBPMRR.close();} catch (Exception e1) {}
            Log.e(TAG, "Exception while writing bpmrr row", e);
            return false;
        }

        return true;
    }

	/**
	 * New appendHeader(), adapted to work with OpenSignals (r)evolution
	 */
    private boolean appendHeader() {

        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String tmpFilePath = context.getFilesDir() + "/" + Constants.TEMP_FILE;
        Date date = new Date();
        Date dateWithoutTime=null;
        OutputStreamWriter out = null;
        BufferedInputStream origin = null;
        BufferedOutputStream dest = null;
        FileInputStream fi = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
            dateWithoutTime = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        try {
            out = new OutputStreamWriter(context.openFileOutput("Bitadroid_Frames" +Constants.TEXT_FILE_EXTENTION, Context.MODE_PRIVATE));
            out.write("# OpenSignals Text File Format\n");
            out.write("# {");
            out.write("\""+device.getAddress()+"\": ");
            out.write("{");
            out.write("\"sensor\": ");
            out.write("[\"RAW\", \"RAW\", \"RAW\", \"RAW\", \"RAW\", \"RAW\"], ");
            out.write("\"device name\": ");
            out.write("\""+device.getAddress()+"\", ");
            out.write("\"column\": ");
            out.write("[\"nSeq\", \"I1\", \"I2\", \"O1\", \"O2\", \"A1\", \"A2\", \"A3\", \"A4\", \"A5\", \"A6\"], ");
            out.write("\"sync interval\": 0, ");
            out.write("\"time\": ");
            out.write("\""+ Calendar.getInstance().getTime()+"\", ");
            out.write("\"comments\": \"\", ");
            out.write("\"device connection\": ");
            out.write("\""+device.getAddress()+"\", ");
            out.write("\"channels\": ");
            out.write(configuration.channelsToString()+", ");
            out.write("\"keywords\": \"\", ");
            out.write("\"mode\": \"regular\", ");
            out.write("\"digital IO\": ");
            out.write("[0, 0, 1, 1], ");
            out.write("\"firmware version\": 128, ");
            out.write("\"device\": \"bitalino_rev\", ");
            out.write("\"position\": 0, ");
            out.write("\"sampling rate\": ");
            out.write(configuration.getSampleRate()+", ");
            out.write("\"label\": ");
            out.write(configuration.getheaderChannelNames()+", ");
            out.write("\"resolution\": ");
            out.write("[4, 1, 1, 1, 1, 10, 10, 10, 10, 6, 6], ");
            out.write("\"date\": ");
            out.write("\""+sdf.format(dateWithoutTime)+"\", ");
            out.write("\"special\": ");
            out.write("[{}, {}, {}, {}, {}, {}]");
            out.write("}}");
            out.write("\n");
            out.write("# EndOfHeader\n");
            out.flush();
            out.close();
            // APPEND DATA
            FileOutputStream outBytes = new FileOutputStream(context.getFilesDir()
                    + "/"+"Bitadroid_Frames" +Constants.TEXT_FILE_EXTENTION, true);
            dest = new BufferedOutputStream(outBytes);
            fi = new FileInputStream(tmpFilePath);

            origin = new BufferedInputStream(fi, BUFFER);
            int count;
            byte data[] = new byte[BUFFER];

            Long tmpFileSize = (new File(tmpFilePath)).length();
            long currentBitsCopied = 0;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
                currentBitsCopied += BUFFER;
                sendPercentageToActivity((int)( currentBitsCopied * 100 / tmpFileSize), STATE_APPENDING_HEADER);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File to write header on, not found", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Write header stream exception", e);
            return false;
        }
        finally{
            try {
                fi.close();
                out.close();
                origin.close();
                dest.close();
                context.deleteFile(Constants.TEMP_FILE);
            } catch (IOException e) {
                try {out.close();} catch (IOException e1) {}
                try {origin.close();} catch (IOException e1) {}
                try {dest.close();} catch (IOException e1) {};
                Log.e(TAG, "Closing streams exception", e);
                return false;
            }
        }
        return true;
    }
    //New method created to set the headers in the location data files
    private boolean appendHeaderLoc() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String tmpFilePath = context.getFilesDir() + "/" + Constants.TEMP_LOC_FILE;
        Date date = new Date();
        Date dateWithoutTime=null;
        OutputStreamWriter out = null;
        BufferedInputStream origin = null;
        BufferedOutputStream dest = null;
        FileInputStream fi = null;
        try {
            out = new OutputStreamWriter(context.openFileOutput("Locations"+Constants.TEXT_FILE_EXTENTION, Context.MODE_PRIVATE));
            out.write("# Locations By Bitadroid\n");
            out.write("# {");
            out.write("\""+device.getAddress()+"\": ");
            out.write("{");
            out.write("\"column\": ");
            out.write("[\"Latitude\", \"Longitude\", \"Altitude\", \"Distance\"], ");
            out.write("\"time\": ");
            out.write("\""+ Calendar.getInstance().getTime()+"\", ");
            out.write("\"comments\": \"\", ");
            out.write("\"device connection\": ");
            out.write("\"date\": ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            try {
                dateWithoutTime = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            out.write("\""+sdf.format(dateWithoutTime)+"\", ");
            out.write("}}");
            out.write("\n");
            out.write("# EndOfHeader\n");
            out.flush();
            out.close();
            // APPEND DATA
            FileOutputStream outBytes = new FileOutputStream(context.getFilesDir()
                    + "/"+"Locations"+Constants.TEXT_FILE_EXTENTION, true);
            dest = new BufferedOutputStream(outBytes);
            fi = new FileInputStream(tmpFilePath);

            origin = new BufferedInputStream(fi, BUFFER);
            int count;
            byte data[] = new byte[BUFFER];

            Long tmpFileSize = (new File(tmpFilePath)).length();
            long currentBitsCopied = 0;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
                currentBitsCopied += BUFFER;
                sendPercentageToActivity((int)( currentBitsCopied * 100 / tmpFileSize), STATE_APPENDING_HEADER);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File to write header on, not found", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Write header stream exception", e);
            return false;
        }
        finally{
            try {
                fi.close();
                out.close();
                origin.close();
                dest.close();
                context.deleteFile(Constants.TEMP_LOC_FILE);
            } catch (IOException e) {
                try {out.close();} catch (IOException e1) {}
                try {origin.close();} catch (IOException e1) {}
                try {dest.close();} catch (IOException e1) {};
                Log.e(TAG, "Closing streams exception", e);
                return false;
            }
        }
        return true;
    }
    //New method created to set the headers in the BPMRR data files
    private boolean appendHeaderBPMRR() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        String tmpFilePath = context.getFilesDir() + "/" + Constants.TEMP_BPMRR_FILE;
        Date date = new Date();
        Date dateWithoutTime=null;
        OutputStreamWriter out = null;
        BufferedInputStream origin = null;
        BufferedOutputStream dest = null;
        FileInputStream fi = null;
        try {
            out = new OutputStreamWriter(context.openFileOutput("PulseMetrics"+Constants.TEXT_FILE_EXTENTION, Context.MODE_PRIVATE));
            out.write("# Pulse Metrics By Bitadroid\n");
            out.write("# {");
            out.write("\""+device.getAddress()+"\": ");
            out.write("{");
            out.write("\"column\": ");
            out.write("[\"RR\", \"BPM\"], ");
            out.write("\"time\": ");
            out.write("\""+ Calendar.getInstance().getTime()+"\", ");
            out.write("\"comments\": \"\", ");
            out.write("\"device connection\": ");
            out.write("\"date\": ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            try {
                dateWithoutTime = sdf.parse(sdf.format(new Date()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            out.write("\""+sdf.format(dateWithoutTime)+"\", ");
            out.write("}}");
            out.write("\n");
            out.write("# EndOfHeader\n");
            out.flush();
            out.close();
            // APPEND DATA
            FileOutputStream outBytes = new FileOutputStream(context.getFilesDir()
                    + "/"+"PulseMetrics"+Constants.TEXT_FILE_EXTENTION, true);
            dest = new BufferedOutputStream(outBytes);
            fi = new FileInputStream(tmpFilePath);

            origin = new BufferedInputStream(fi, BUFFER);
            int count;
            byte data[] = new byte[BUFFER];

            Long tmpFileSize = (new File(tmpFilePath)).length();
            long currentBitsCopied = 0;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
                currentBitsCopied += BUFFER;
                sendPercentageToActivity((int)( currentBitsCopied * 100 / tmpFileSize), STATE_APPENDING_HEADER);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "File to write header on, not found", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Write header stream exception", e);
            return false;
        }
        finally{
            try {
                fi.close();
                out.close();
                origin.close();
                dest.close();
                context.deleteFile(Constants.TEMP_BPMRR_FILE);
            } catch (IOException e) {
                try {out.close();} catch (IOException e1) {}
                try {origin.close();} catch (IOException e1) {}
                try {dest.close();} catch (IOException e1) {};
                Log.e(TAG, "Closing streams exception", e);
                return false;
            }
        }
        return true;
    }
    //New method based on the old one by Carlos Marten, compresses all 3 files
    private Boolean compressFileNew(){
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy");
		SimpleDateFormat stf=new SimpleDateFormat("hh:mm a");
		Date dateWithoutTime = null;
		Date timewithoutDate=null;
		try {
			dateWithoutTime = sdf.parse(sdf.format(new Date()));
			timewithoutDate=stf.parse(stf.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		String directoryAbsolutePath = Environment.getExternalStorageDirectory().toString()+ Constants.APP_DIRECTORY;
		File root = new File(directoryAbsolutePath);
		String zipFileName = recordingName+"_"+sdf.format(dateWithoutTime)+"_"+stf.format(timewithoutDate)+"_"+
				Constants.ZIP_FILE_EXTENTION;
		root.mkdirs();
		try {
			FileOutputStream fos = new FileOutputStream(root +"/"+ zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			String file1Name = "Bitadroid_Frames"+ Constants.TEXT_FILE_EXTENTION;
			String file2Name = "Locations" + Constants.TEXT_FILE_EXTENTION;

			//String file3Name = "folder/file3.txt";

			addToZipFile(file1Name, zos);
			addToZipFile(file2Name, zos);
            if(pulseMetricsON){
                String file3Name = "PulseMetrics" + Constants.TEXT_FILE_EXTENTION;
                addToZipFile(file3Name,zos);
            }
			//addToZipFile(file3Name, zos);
			zos.close();
			fos.close();
            context.deleteFile("Frames" + Constants.TEXT_FILE_EXTENTION);
            context.deleteFile("Locations"+ Constants.TEXT_FILE_EXTENTION);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return  true;
	}

	public void addToZipFile(String fileName, ZipOutputStream zos) throws FileNotFoundException, IOException {

		Log.d(TAG,"Writing '" + fileName + "' to zip file");

		File file = new File(context.getFilesDir() + "/" + fileName);
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(fileName);
		zos.putNextEntry(zipEntry);

		byte[] bytes = new byte[BUFFER];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zos.write(bytes, 0, length);
		}

		zos.closeEntry();
		fis.close();

	}


    /**
	 * Returns true if compressed successfully and false otherwise. NOT USED
	 */
	private boolean compressFile(){
		
		BufferedInputStream origin = null;
        BufferedInputStream origin2 = null;
		ZipOutputStream out = null;
		DateFormat dateFormat = DateFormat.getDateTimeInstance();
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy");
		SimpleDateFormat stf=new SimpleDateFormat("hh:mm a");
		Date dateWithoutTime = null;
		Date timewithoutDate=null;
		try {
			dateWithoutTime = sdf.parse(sdf.format(new Date()));
			timewithoutDate=stf.parse(stf.format(new Date()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//this.creationDate=sdf.format(dateWithoutTime);
			String zipFileName = recordingName+"_"+sdf.format(dateWithoutTime)+"_"+stf.format(timewithoutDate)+"_"+
                    Constants.ZIP_FILE_EXTENTION;
			String fileName = recordingName+dateFormat.format(date) + Constants.TEXT_FILE_EXTENTION;
            String filenameLoc=recordingName+dateFormat.format(date)+"_LOCATIONS" + Constants.TEXT_FILE_EXTENTION;
            //String filenameLoc="Locations_"+sdf.format(dateWithoutTime)+Constants.TEXT_FILE_EXTENTION;
			String directoryAbsolutePath = Environment.getExternalStorageDirectory().toString()+ Constants.APP_DIRECTORY;
			File root = new File(directoryAbsolutePath);
			root.mkdirs();
			
		try {	
			FileOutputStream dest = new FileOutputStream(root +"/"+ zipFileName);
					
			out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];

			FileInputStream fi = new FileInputStream(context.getFilesDir() + "/" + fileName);
            FileInputStream fi2 = new FileInputStream(context.getFilesDir() + "/" + filenameLoc);
			origin = new BufferedInputStream(fi, BUFFER);
            origin2=new BufferedInputStream(fi2,BUFFER);
			
			ZipEntry entry = new ZipEntry(fileName.substring(fileName.lastIndexOf("/") + 1));
            ZipEntry entry2 = new ZipEntry(filenameLoc.substring(filenameLoc.lastIndexOf("/") + 1));
			out.putNextEntry(entry);
            out.putNextEntry(entry2);
			int count;
			int count2;
			
			Long recordingSize = (new File(context.getFilesDir() + "/" + fileName)).length();
			long currentBitsCompressed = 0;
			while ((count = origin.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count);
				currentBitsCompressed += BUFFER;
				sendPercentageToActivity((int)( currentBitsCompressed * 100 / recordingSize), STATE_COMPRESSING_FILE);
			}
			while ((count2 = origin2.read(data, 0, BUFFER)) != -1) {
				out.write(data, 0, count2);
				currentBitsCompressed += BUFFER;
				sendPercentageToActivity((int)( currentBitsCompressed * 100 / recordingSize), STATE_COMPRESSING_FILE);
			}
			context.deleteFile(recordingName+dateFormat.format(date) + Constants.TEXT_FILE_EXTENTION);
            context.deleteFile(recordingName+dateFormat.format(date)+"_LOCATIONS"+ Constants.TEXT_FILE_EXTENTION);
			// Tells the media scanner to scan the new compressed file, so that
			// it is visible for the user via USB without needing to reboot
			// device because of the MTP protocol
			Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			intent.setData(Uri.fromFile(new File(root + "/" + zipFileName)));
			context.sendBroadcast(intent);
			
		} catch (Exception e) {
			context.deleteFile(recordingName + Constants.TEXT_FILE_EXTENTION);
			Log.e(TAG, "Exception while zipping", e);
			return false;
		}
		finally{
			try {
				if(origin!=null&&out!=null) {
					origin.close();
					out.close();
				}
				if(origin2!=null&&out!=null) {
					origin2.close();
					out.close();
				}
			} catch (IOException e) {
				try {out.close();} catch (IOException e1) {}
				Log.e(TAG, "Exception while closing streams", e);
				return false;
			}	
		}
		return true;
	}
	
	/**
	 * Used to send updates of the percentage of adding the header or compressing the file
	 * to the client, to keep him informed while waiting
	 */
	private void sendPercentageToActivity(int percentage, int state) {
		try {
			this.client.send(Message.obtain(null, MSG_PERCENTAGE, percentage, state));
		} catch (RemoteException e) {
			Log.e(TAG, "Exception sending percentage message to activity", e);
		}
	}
	
	/**
	 * Returns true if writers were closed properly. False if an exception was
	 * caught closing them Modified
	 */
	public boolean closeWriters(){
		try {
			bufferedWriter.flush();
            bufferedWriterLocation.flush();
            bufferedWriterBPMRR.flush();
			bufferedWriter.close();
            bufferedWriterLocation.close();
            bufferedWriterBPMRR.close();
			outStreamWriter.close();
		} catch (IOException e) {
			try {bufferedWriter.close();} catch (IOException e1) {}
            try {bufferedWriterLocation.close();} catch (IOException e1) {}
            try {bufferedWriterBPMRR.close();} catch (IOException e1) {}
			try {outStreamWriter.close();} catch (IOException e2) {}
			Log.e(TAG, "Exception while closing Writers", e);
			return false;
		}
		return true;
	}
	
	/**
	 * Saves and compress a recording. Returns true if the writing and the
	 * compression were successful or false if either one of them failed MODIFIED
	 */
	public boolean saveAndCompressFile(Messenger client) {
		this.client = client;
		if(!enoughStorageAvailable())
			return false;
        if (!appendHeaderLoc())
            return false;
		if (!appendHeader())
			return false;
        if(!appendHeaderBPMRR())
            return false;
        return compressFileNew();
    }

	/**
	 * Returns the internal storage available in bytes
	 */
	@SuppressWarnings("deprecation")
	public long internalStorageAvailable() {
		StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
		long free = ((long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize());// in Bytes [/1048576 -> in MB]
		return free;
	}

	/**
	 * Returns the external storage available in bytes
	 */
	@SuppressWarnings("deprecation")
	public long externalStorageAvailable() {
		StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
		long free = ((long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize());// in Bytes [/1048576 -> in MB]
		return free;
	}

	/**
	 * Checks whether there is enough internal and external storage available to
	 * save the recording.
	 * 
	 * Returns true if there is enough storage or false otherwise
	 */
	private boolean enoughStorageAvailable() {
		Long tmpFileSize = (new File(context.getFilesDir() + "/" + Constants.TEMP_FILE)).length();
		Log.i(TAG, "text file size: " + tmpFileSize);
		Log.i(TAG, "internal storage available: " + internalStorageAvailable());
		Log.i(TAG, "external storage available: " + externalStorageAvailable());
		boolean isEnough = false;
		
		if (internalStorageAvailable() > (tmpFileSize * 2 + 2 * 1048576)) // 2*tmpFile + 2MB
			isEnough = true;
		else {
			Log.e(TAG, "not enough internal storage to save raw recording");
			isEnough = false;
		}
		if(isEnough){
			if (externalStorageAvailable() > (tmpFileSize / 4))// compressed, weights 1/4 of the space
				isEnough = true;
			else {
				Log.e(TAG, "not enough external storage to save compressed recording");
				isEnough = false;
			}
		}
		
		return isEnough;
	}

	/**
	 * Sets the duration of the recording
	 */
	public void setDuration(String _duration) {
		this.duration = _duration;
	}


}
