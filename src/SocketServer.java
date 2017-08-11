import java.io. * ;
import java.net. * ;
import java.util. * ;

public class SocketServer {
	public static Vector<SocketServer_Thread> v = new Vector<SocketServer_Thread>(); 
	
	public static void main(String[] args) {
		int i = 1;
		try 
		{	
			ServerSocket s = new ServerSocket(8821); //用8821 port
			for (;;) {
				System.out.println("Server start, waiting for client...");
				Socket incoming = s.accept();
				System.out.println("Client ID : " + i + ", Connection Started");
				new SocketServer_Thread(incoming, i, v).start();
				i++;
			}
		} catch(Exception e) {
			System.out.println("Exception in line 19 : " + e);
		}
	}
}

class SocketServer_Thread extends Thread {
	private Socket incoming;
	private int counter;
	private Vector v;
	private String fileDir = "/home/hduer/Desktop/MalwareSample/"; //要傳送哪個目錄
	
	public SocketServer_Thread(Socket i, int c, Vector v) {
		incoming = i;
		counter = c;
		this.v = v;
	}
	
	public void connectionEnded() { 
		v.remove(this);
		System.out.println("ID : " + counter + ", Connection Ended");
	}
	
	public void run() {
		try {
			v.add(this);
			File fd = new File(fileDir); //某目錄
			File[] fdlist = fd.listFiles();
			
			try 
			{   //將要傳的檔案數量給Client端，用以切斷接收
				DataOutputStream ps = new DataOutputStream(incoming.getOutputStream()); 
				ps.writeInt(fdlist.length);
				ps.flush(); 
				
				for (int i = 0; i < fdlist.length; i++) 
					sendFile(fdlist[i].getName());
				
				
				System.out.println("Transmission done, start analysis");
				int k = fdlist.length/2;
					
				while( k < fdlist.length ){
						ProcessBuilder pb = new ProcessBuilder("./submit.py",fdlist[k].getPath());
						pb.directory(new File("/home/hduer/cuckoo/utils/"));
						Process p1 =pb.start();
						String line ="";
						BufferedReader p_in = new BufferedReader(new InputStreamReader(p1.getInputStream()));
						while((line=p_in.readLine())!=null){
							System.out.println(line);
						}
						p_in.close();
						k++;
				}
					
				//接收報告
				DataInputStream inputStream = new DataInputStream(incoming.getInputStream());
				long len;
				long delta;
				int bufferSize=65536;
				byte[] buf = new byte[bufferSize];
				
				//此迴圈表示要接收幾個檔案
				for(int i = 0 ; i < fdlist.length/2 ; i++){	 
					System.out.println("i="+i);
					String savePath = "/home/hduer/Desktop/result/"+((fdlist.length/2)+i)+"/"; //路徑重設
					File tempfile = new File(savePath);
					tempfile.mkdir();
					int passedlen = 0; //歸零
					savePath += inputStream.readUTF();

					DataOutputStream fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
					len = inputStream.readLong();
					System.out.println("文件的長度為:" + len + "");
					System.out.println("開始接收文件第 "+ (i + 1) +" 個文件!" + "");
					
					while (true) {
						int read = 0;
						if (inputStream != null) {
							delta = len - passedlen;
							if(delta>bufferSize)
								read = inputStream.read(buf);
							else
								read=inputStream.read(buf,0,(int)delta);
						}
						
						passedlen += read;
						System.out.println("read="+read);
						fileOut.write(buf, 0, read);
						
						if (passedlen == len) //如果長度已經足了,離開準備關閉檔案串流
							break;
					}
					System.out.println("接收完成，文件存為" + savePath + "\n");
					
					//the second
					savePath = "/home/hduer/Desktop/result/"+(fdlist.length/2+i)+"/"; //路徑重設
					passedlen = 0; //歸零
					savePath += inputStream.readUTF();

					fileOut = new DataOutputStream(new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));
					len = inputStream.readLong();
					System.out.println("文件的長度為:" + len + "");
					System.out.println("開始接收文件第 "+ (i + 1) +" 個文件!" + "");

					while (true) {
						int read = 0;
						if (inputStream != null) {
							delta= len-passedlen;
						
							if(delta>bufferSize)
								read = inputStream.read(buf);
							else
								read=inputStream.read(buf,0,(int)delta);
						}
						passedlen += read;
						fileOut.write(buf, 0, read);
						
						if (passedlen == len) //如果長度已經足了,離開準備關閉檔案串流
							break;
					}
					
					System.out.println("接收完成，文件存為" + savePath + "\n");
					fileOut.close();
				}
			} catch(Exception e) {
				System.out.println(e);
			}
			incoming.close();
			connectionEnded(); 
		} catch(Exception e) {
			System.out.println("Exception in line 87 : " + e);
		}
	}
	
	/* 
	 * 傳檔的function
	 */
	public void sendFile(String fileName) {
		try { 
			String filePath = fileDir + fileName;
			File fi = new File(filePath);
			System.out.println("文件長度:" + (int) fi.length()); 
			System.out.println("文件名稱:" + (String) fi.getName()); 

			DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
			DataOutputStream ps = new DataOutputStream(incoming.getOutputStream()); 
			ps.writeUTF(fi.getName());
			ps.flush();
			ps.writeLong((long) fi.length()); //將即將要傳的檔案大小傳給Client
			ps.flush();
			
			int bufferSize = 65536; //設定一個BUFFER大小
			byte[] buf = new byte[bufferSize];
			System.out.println("文件傳送中..."); //一直循環到brake;強制離開
			
			while (true) {
				int read = 0;
				if (fis != null) 
					read = fis.read(buf);
				
				if (read == -1) 
					break;
				
				ps.write(buf, 0, read);
				System.out.println("read="+read); 
			}
			
			ps.flush();  // 直到socket超時，導致資料不完整。 
			fis.close(); //	incoming.close();
			System.out.println("文件傳輸完成"); 
		} catch(Exception e) {
			System.out.println("Exception in line 87 : " + e);
		} 
	}	
}