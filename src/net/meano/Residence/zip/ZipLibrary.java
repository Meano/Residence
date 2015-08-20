package net.meano.Residence.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.meano.Residence.Residence;
import org.bukkit.World;

public class ZipLibrary {
	private static File BackupDir = new File(Residence.getDataLocation(), "Backup");

	public static void backup() throws IOException {
		try {
			BackupDir.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		// Generate the proper date for the backup filename
		//Bukkit.getLogger().info("RS11111111111111");
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		File fileZip = new File(BackupDir, dateFormat.format(date) + ".zip");
		//Bukkit.getLogger().info("RS22222222222");
		// Create the Source List, and add directories/etc to the file.
		List<File> sources = new ArrayList<File>();
		//Bukkit.getLogger().info("RS33333333333");
		File saveFolder = new File(Residence.getDataLocation(), "Save");
		File worldFolder = new File(saveFolder, "Worlds");
		if (!saveFolder.isDirectory()) {
			return;
		}
		//Bukkit.getLogger().info("RS44444444444444");
		File saveFile;
		for (World world : Residence.getServ().getWorlds()) {
			saveFile = new File(worldFolder, "res_" + world.getName() + ".yml");
			if (saveFile.isFile()) {
				sources.add(saveFile);
			}
		}
		//Bukkit.getLogger().info("RS55555555555555");
		packZip(fileZip, sources);
	}

	private static void packZip(File output, List<File> sources)
			throws IOException {
		ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(output));
		zipOut.setLevel(Deflater.DEFAULT_COMPRESSION);

		for (File source : sources) {
			if (source.isDirectory()) {
				zipDir(zipOut, "", source);
			} else {
				zipFile(zipOut, "", source);
			}
		}

		zipOut.flush();
		zipOut.close();
	}

	private static String buildPath(String path, String file) {
		if (path == null || path.isEmpty()) {
			return file;
		}

		return path + File.separator + file;
	}

	private static void zipDir(ZipOutputStream zos, String path, File dir)
			throws IOException {
		if (!dir.canRead()) {
			return;
		}

		File[] files = dir.listFiles();
		path = buildPath(path, dir.getName());

		for (File source : files) {
			if (source.isDirectory()) {
				zipDir(zos, path, source);
			} else {
				zipFile(zos, path, source);
			}
		}
	}

	private static void zipFile(ZipOutputStream zos, String path, File file)
			throws IOException {
		if (!file.canRead()) {
			return;
		}

		zos.putNextEntry(new ZipEntry(buildPath(path, file.getName())));

		FileInputStream fis = new FileInputStream(file);
		byte[] buffer = new byte[4092];
		int byteCount = 0;

		while ((byteCount = fis.read(buffer)) != -1) {
			zos.write(buffer, 0, byteCount);
		}

		fis.close();
		zos.closeEntry();
	}
}
