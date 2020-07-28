import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import com.github.jscancella.hash.Hasher;

public class ThreadSafeSHA3256Hasher implements Hasher {
  private static final int _64_KB = 1024 * 64;
  private static final int CHUNK_SIZE = _64_KB;
  private MessageDigest messageDigestInstance;

  @Override
  public String hash(final Path path) throws IOException{
	try {
      final MessageDigest threadLocalMessageDigest = MessageDigest.getInstance("SHA3-256");
      updateMessageDigest(path, threadLocalMessageDigest);
      return formatMessageDigest(threadLocalMessageDigest);
    } catch (NoSuchAlgorithmException e) {
      throw new IOException(e); //rethrow the error as IOException
    }
  }
  
  @Override
  public void update(byte[] bytes){
    messageDigestInstance.update(bytes);
  }
  
    @Override
  public String getHash(){
    return formatMessageDigest(messageDigestInstance);
  }

  @Override
  public void reset(){
    messageDigestInstance.reset();
  }
  
    @Override
  public String getBagitAlgorithmName(){
    return "sha3256";
  }
  
  private static void updateMessageDigest(final Path path, final MessageDigest messageDigest) throws IOException{
    try(InputStream inputStream = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ))){
      final byte[] buffer = new byte[CHUNK_SIZE];
      int read = inputStream.read(buffer);

      while(read != -1){
        messageDigest.update(buffer, 0, read);
        read = inputStream.read(buffer);
      }
    }
  }
  
  private static String formatMessageDigest(final MessageDigest messageDigest){
    try(Formatter formatter = new Formatter()){
      for (final byte b : messageDigest.digest()) {
        formatter.format("%02x", b);
      }
      
      return formatter.toString();
    }
  }
  
  @Override
  public void initialize() throws NoSuchAlgorithmException{
    messageDigestInstance = MessageDigest.getInstance("SHA3-256");
  }

}