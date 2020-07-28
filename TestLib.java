/*
 * USAGE UNIX: javac -cp '.:lib/bagging-4.3.jar:lib/slf4j-api-1.7.30.jar:lib/slf4j-simple-1.6.1.jar' TestLib.java
 *             java  -cp '.:lib/bagging-4.3.jar:lib/slf4j-api-1.7.30.jar:lib/slf4j-simple-1.6.1.jar' TestLib
 *
 * NOTES:      tested only on debian 10 machine
*/

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;
import javax.xml.validation.Validator;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jscancella.domain.Bag;
import com.github.jscancella.domain.BagBuilder;
import com.github.jscancella.hash.internal.FileCountAndTotalSizeVistor;
import com.github.jscancella.hash.BagitChecksumNameMapping;

public class TestLib {
	
	private static final Logger logger = LoggerFactory.getLogger(TestLib.class);
	private static final String absolute_dir = System.getProperty("user.dir");
	
	public static String getFileName(String sPath){
		Path path = Paths.get(sPath);
		String name = path.getFileName().toString();
		return name;
	}
	
	public static String getCurrentISODate(String format){
		DateFormat df = new SimpleDateFormat(format);
		String currentISODate = df.format(new Date());
		return currentISODate;
	}
	
	public static String createBagItName(String ie_path_import){
		String name = getFileName(ie_path_import);
		return name + getCurrentISODate("_yyyy-MM-dd_HH-mm-ss");
	}
	
	
	public static void main(String[] args) throws IOException{
		// variables
		String ie_path_import        = "import/oaiobj71291926/data";
		String slubbagit_path_export = "export";
		
		// create bagit name directory for export
		String ie_path_import_no_data = ie_path_import;
		ie_path_import_no_data = ie_path_import_no_data.substring(0, ie_path_import_no_data.length() - 5);
		String ie_name = createBagItName(ie_path_import_no_data);
		slubbagit_path_export = slubbagit_path_export + File.separator + ie_name;
		
		Path bagNewDir = Paths.get(slubbagit_path_export);	// NOTE: the data directory MUST be included as this is part of the relative path
		
		Path payloadFiles = Paths.get(ie_path_import);
		
		//create new thread safe hasher
		ThreadSafeMD5Hasher newMd5Hasher = new ThreadSafeMD5Hasher();
		BagitChecksumNameMapping.add(newMd5Hasher.getBagitAlgorithmName(), newMd5Hasher);
		
		ThreadSafeSHA512Hasher newSHA512Hasher = new ThreadSafeSHA512Hasher();
		BagitChecksumNameMapping.add(newSHA512Hasher.getBagitAlgorithmName(), newSHA512Hasher);
		
		//ThreadSafeSHA3256Hasher newSha3256Hasher = new ThreadSafeSHA3256Hasher();	
		//BagitChecksumNameMapping.add(newSha3256Hasher.getBagitAlgorithmName(), newSha3256Hasher);
	
		BagBuilder builder = new BagBuilder();
		builder.addAlgorithm("md5")
			.addAlgorithm("sha512")
			//.addAlgorithm("sha3256")	// ERROR com.github.jscancella.domain.BagBuilder - [sha3256] is not supported so it will be ignored. Please add an implementation to BagitChecksumNameMapping.java if you wish to use [sha3256]
			.addMetadata("foo", "bar")			
			.addPayloadFile(payloadFiles)
			.addTagFile(Paths.get(absolute_dir + "/import/meta"))
			.bagLocation(bagNewDir)
			.write();
		
        System.exit(0);
    }
}
