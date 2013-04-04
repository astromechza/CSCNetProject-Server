package database;

import java.io.File;

import com.almworks.sqlite4java.SQLiteConnection;
import com.almworks.sqlite4java.SQLiteException;
import com.almworks.sqlite4java.SQLiteJob;
import com.almworks.sqlite4java.SQLiteQueue;
import com.almworks.sqlite4java.SQLiteStatement;

public class Database 
{
	private File file;
	private SQLiteQueue dbQueue;
	
	public Database(File file)
	{
		this.file = file;
		dbQueue = new SQLiteQueue(file);
		dbQueue.start();
	}
	
	public void stop()
	{
		dbQueue.stop(true);		
	}
	
	public void createTable(Storable storable)
	{
		SingleJob createtableJob = new SingleJob(storable, null) 
		{			
			protected Storable job(SQLiteConnection db) throws SQLiteException {
				SQLiteStatement s = db.prepare("CREATE TABLE IF NOT EXISTS " + storable.getTableName() + "(" + storable.getColumnDefs() + ")");
				try
				{
					s.stepThrough();
				}
				finally
				{
					s.dispose();
				}
				return null;
				
			}			
		};
		
		dbQueue.execute(createtableJob).complete();		
	}
	
	public void save(Storable storable) {
		
    	dbQueue.execute(new SingleJob(storable, null) 
    	{
    		protected Storable job(SQLiteConnection db) throws SQLiteException {
    			SQLiteStatement s = db.prepare(storable.insertStmt());
    			storable.bindToStmt(s);
				try
				{
					s.stepThrough();
				}
				finally
				{
					s.dispose();
				}
    			return null;
    		}
    	}).complete();
    }
	
	
	/* JOB CLASSES
	 * These run tasks in the DBqueue and return nice results hopefully bound to objects; 
	 */
	private abstract class SingleJob extends SQLiteJob<Storable> {
    	public Storable storable;
    	public Long id;
    	
    	public SingleJob(Storable storable, Long id) {
    		this.storable = storable;
    		this.id = id;
    	}
    }
	
	
}
