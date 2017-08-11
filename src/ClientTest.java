import java.io. * ;
import java.net. * ;
import java.util. * ;
import java.text. * ;

public class ClientTest {
	public static void main(String[] args) {
		DataInputStream inputStream = null;
		DataOutputStream outputStream = null; 
		String fileDir = "/home/a110605/cuckoo/malwareDir/"; //要接收到哪個目錄
		
		try {
			Socket ss = new Socket("140.112.107.43", 8821);
			String savePath = fileDir;
			int sendnum;
			int bufferSize = 65536;
			byte[] buf = new byte[bufferSize];
			long passedlen = 0;
			long len = 0;
			long delta;
			int id;
			inputStream = new DataInputStream(new BufferedInputStream(ss.getInputStream())); 
			outputStream = new DataOutputStream(new BufferedOutputStream(ss.getOutputStream())); 

			//取得檔案數量
			sendnum = inputStream.readInt();
			System.out.println("要接收"+ sendnum +"個文件");
			
			//這裡行使迴圈表示要接收幾個檔案
			for (int i = 0; i < sendnum; i++) { 
				savePath = fileDir; //路徑重設
				passedlen = 0; //歸零
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
					fileOut.write(buf, 0, read);
					
					 //如果長度已經足了,離開準備關閉檔案串流
					if (passedlen == len) 
						break;
				}
				System.out.println("接收完成，文件存為" + savePath + "\n");
				fileOut.close();
			}

			System.out.println("all files is in the dir");
			ProcessBuilder pb = new ProcessBuilder("./submitall.sh");
			pb.directory(new File("/home/a110605/cuckoo/cuckoo-master/"));
			Process p1 =pb.start();
		
			String line ="";
			BufferedReader p_in = new BufferedReader(new InputStreamReader(p1.getInputStream()));
			
			while((line=p_in.readLine())!=null){
				System.out.println(line);
			}
			p_in.close();

			//transmit report.html & report.json back to server
			int k=1;
			while( k <= sendnum ){
				String path1="/home/a110605/cuckoo/cuckoo-master/storage/analyses/"+k+"/reports/report.html";
				String path2="/home/a110605/cuckoo/cuckoo-master/storage/analyses/"+k+"/reports/report.json";
				File file = new File(path1);
				File file2 = new File(path2);
				
				if (file.exists() && file2.exists()){
					try { //傳檔的寫法
						String filePath = path1;
						File fi = new File(filePath);
						System.out.println("文件長度:" + (int) fi.length()); // public Socket accept() throws
						System.out.println("文件名稱:" + (String) fi.getName()); // public Socket accept() throws

						DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
						outputStream.writeUTF(fi.getName());
						outputStream.flush();
						outputStream.writeLong((long) fi.length()); //將即將要傳的檔案大小傳給Client
						outputStream.flush();
						System.out.println("文件傳送中..."); //一直循環到brake;強制離開
						
						while (true) {
							int read = 0;
							
							if (fis != null) 
								read = fis.read(buf);
							
							if (read == -1) 
								break;
							
							outputStream.write(buf, 0, read);
							System.out.println("read="+read);
						}
					
						outputStream.flush(); // 直到socket超時，導致資料不完整。 
						System.out.println("文件傳輸完成"); //傳檔寫法end

					} catch(Exception e) {
						System.out.println("Exception in line 87 : " + e);
					} 
				
					//second file
					try { //傳檔的寫法
						String filePath = path2;
						File fi = new File(filePath);
						System.out.println("文件長度:" + (int) fi.length()); // public Socket accept() throws
						System.out.println("文件名稱:" + (String) fi.getName()); // public Socket accept() throws

						DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
						outputStream.writeUTF(fi.getName());
						outputStream.flush();
						outputStream.writeLong((long) fi.length()); //將即將要傳的檔案大小傳給Client
						outputStream.flush();
						System.out.println("文件傳送中..."); //一直循環到brake;強制離開
						
						while (true) {
							int read = 0;
							
							if (fis != null) 
								read = fis.read(buf);
							
							if (read == -1) 
								break;
							
							outputStream.write(buf, 0, read);
							System.out.println("read="+read);
						}
						outputStream.flush(); // 直到socket超時，導致資料不完整。 
						fis.close(); //			incoming.close();
						System.out.println("文件傳輸完成"); 
						
					} catch(Exception e) {
						System.out.println("Exception in line 87 : " + e);
					}
				k++;
				}else {
					
					try{
						Thread.sleep(10000);
						System.out.println("k= "+k+" sleep 10 seconds");
					}catch(Exception e){
						System.out.println(e);
					}
					
				}
			}
		} catch(Exception e) {
			System.out.println("接收消息錯誤" + e);
			return;
		}
	}
}