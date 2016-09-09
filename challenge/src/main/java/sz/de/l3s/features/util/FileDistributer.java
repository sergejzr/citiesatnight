package sz.de.l3s.features.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;



public class FileDistributer {
	

	
	private File outdir;
	private URL outurl;
	String[] chunks=new String[4];
	private String fullid;
	private int namelength;
	
	public URL extendURL(String ext) throws MalformedURLException
	{
		return extendURL(outurl,ext);
	}
	
	public URL extendURL(URL parent,String ext) throws MalformedURLException
	{
		URL ret=parent;
		
		for(String chunk:chunks)
		{
			ret=new URL(ret, chunk);
		}
		ret=new URL(ret,fullid+ext);
		return ret;
	}
	
	
	
	public File extendFile(String ext)
	{
		return extendFile(outdir,ext);
	}
	public File extendFile(File dir,String ext)
	{
File ret=dir;
		
		for(String chunk:chunks)
		{
			ret=new File(ret, chunk);
		}
		ret=new File(ret.getAbsoluteFile(),fullid+ext);
		return ret;
	}
	
	public FileDistributer(String id, String path,boolean create)
	{
		
		this(id,new File(path),create);
	}
	private void calculateOutfolder(String id) {
		id = "00000000000000000000000000000000000" + id;
		id = id.substring(id.length() - namelength);
		
		int chnunklength = namelength/3;

		String parts[] = new String[chnunklength];
		
		for(int i=0;i<chnunklength;i++)
		{
			
			parts[i]=id.substring(3*i,3*i+3);
		}
		
		fullid=id;

		chunks=parts;
	}
	public FileDistributer(String id, URL path)
	{
		this(id, path,12);
	}
	public FileDistributer(String id, URL path, int namelength)
	{
		this.namelength=namelength;
		calculateOutfolder(id);
		 
		 
	}
	public FileDistributer(String pid, File imagedir, boolean create) 
	{
		this(pid, imagedir,4,create);
	}
	
	
	public FileDistributer(String pid, File imagedir, int namelength, boolean create) {
		this.namelength=namelength;
		calculateOutfolder(pid);
		outdir=imagedir;
		
		File f=extendFile("");
		if(create){
		f.getParentFile().mkdirs();
		}
		
	}
/*
	private File getPath(String id, String path) {
		return getPath(id, path, false);
	}
	*/
	/*
public File extendFile(String extention)
{
	return new File(outdir.toString()+extention);
}
*/

/*
	private File getPath(String id, String path, boolean create) {
		id = "0000000000000" + id;
		id = id.substring(id.length() - 12);

		String parts[] = new String[4];

		parts[0] = id.substring(0, 3);
		parts[1] = id.substring(3, 6);
		parts[2] = id.substring(6, 9);
		parts[3] = id.substring(9, 12);

		File outdir = new File(path);

		for (String part : parts) {
			outdir = new File(outdir, part);
		}
		if (create) {
			outdir.mkdirs();
		}

		outdir = new File(outdir, id);

		return outdir;
	}
*/
	public static void main(String[] args) {
		FileDistributer fd=new FileDistributer("123456789abc", "E:/testpic/", true);
		
		System.out.println(fd.extendFile(".jpg"));
	}
	
	
	

	public synchronized void removeEmptyFolders() {

		try {
			
			File imgdir = extendFile(outdir,"").getParentFile();
			FileUtils.cleanDir(imgdir, false);
			File parent = imgdir.getParentFile();
			imgdir.delete();
		
			int stop=4;

			while(true)

			{
				String[] list = parent.list();
				if(list==null) return;
				if(list.length!=0) return;
				File tmp = parent.getParentFile();
				if(tmp.getName().length()!=3){return;}
				parent.delete();
				parent=tmp;
				stop--;
				if(stop<=0) return;
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
