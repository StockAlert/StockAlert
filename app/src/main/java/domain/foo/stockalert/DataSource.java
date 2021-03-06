package domain.foo.stockalert;
/**
 * Jan Paul Schulz
 * Nick Herold
 * Hauke Hoppe
 */



import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.content.ContentValues;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.List;


public class DataSource {
	
	private static final String LOG_TAG = DataSource.class.getSimpleName();
    private static DataSource instance;
    private static Context context;
    private SQLiteDatabase database;
    private DBHelper dbHelper;
	
	  //string arrays, containing the coloumns of each table
    private String[] columns_stock_list = {
            DBHelper.stock_id,
              DBHelper.stock_value,
              DBHelper.stock_name,
              DBHelper.stock_timezone,
              DBHelper.stock_date,

      };

    private String[] columns_alert_list = {
            DBHelper.alert_id,
            DBHelper.stock_pricelimit,
            DBHelper.stock_pricelimit1,
            DBHelper.stock_id
            //DBHelper.stock_alert,
    };

    private String[] columns_observation_list = {
            DBHelper.observation_id,
            DBHelper.observation_open,
            DBHelper.observation_high,
            DBHelper.observation_low,
            DBHelper.observation_close,
            DBHelper.observation_date,
            DBHelper.observation_volume,
            DBHelper.stock_id,

    };


    public static DataSource getInstance(Context context){
        if(instance==null) {
            instance = new DataSource(context);
        }
        return instance;
    }

    private DataSource(Context context) {
        Log.d(LOG_TAG, "Unsere DataSource erzeugt jetzt den dbHelper.");
        dbHelper = new DBHelper(context); //a instance of DbHelper is initialized
    }

    //method to open the database
    public void open() {
        Log.d(LOG_TAG, "Eine Referenz auf die Datenbank wird jetzt angefragt.");
        database = dbHelper.getWritableDatabase();
        Log.d(LOG_TAG, "Datenbank-Referenz erhalten. Pfad zur Datenbank: " + database.getPath());
    }

    //method to close the database
    public void close() {
        dbHelper.close();
        Log.d(LOG_TAG, "Datenbank mit Hilfe des DbHelpers geschlossen.");
    }

    public Equity createEquity(String symbol, double price, String date, String timezone){
        ContentValues columns_entry_list = new ContentValues();
        columns_entry_list.put(DBHelper.stock_value, price);
        columns_entry_list.put(DBHelper.stock_name, symbol);
        columns_entry_list.put(DBHelper.stock_date, date);
        columns_entry_list.put(DBHelper.stock_timezone, timezone);




        long insertId = database.insert(DBHelper.table_stock, null, columns_entry_list);

        Cursor cursor = database.query(DBHelper.table_stock, columns_stock_list, DBHelper.stock_id + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Equity equity = cursorToEntry(cursor);
        cursor.close();

        return equity;

	  }

	  public boolean insertEquity(Equity eq){
            boolean success = true;
          ContentValues columns_entry_list = new ContentValues();
          columns_entry_list.put(DBHelper.stock_value, eq.getLatestClose());
          columns_entry_list.put(DBHelper.stock_name, eq.getSymbol());
          columns_entry_list.put(DBHelper.stock_date, eq.getLatestDate());
          columns_entry_list.put(DBHelper.stock_timezone, eq.getTimezone());
          long insertId1 = database.insert(DBHelper.table_stock, null, columns_entry_list);

          if(insertId1!=-1){
              for (Equity.Observation ob: eq.getStock()
                      ) {
                  ContentValues columns_entry_list_observation = new ContentValues();
                  columns_entry_list_observation.put(DBHelper.observation_open, ob.open);
                  columns_entry_list_observation.put(DBHelper.observation_high, ob.high);
                  columns_entry_list_observation.put(DBHelper.observation_low, ob.low);
                  columns_entry_list_observation.put(DBHelper.observation_close, ob.close);
                  columns_entry_list_observation.put(DBHelper.observation_date, ob.printDatetime());
                  columns_entry_list_observation.put(DBHelper.observation_volume, ob.volume);
                  columns_entry_list_observation.put(DBHelper.stock_id, insertId1);
                  long insertId2 = database.insert(DBHelper.table_observation, null, columns_entry_list_observation);
                  if(insertId2==-1)success=false;
              }
          }else success=false;

          return success;
      }


//    public Equity.Observation createObservation(double open, double high, double low, double close, double volume, String date, long stock_id){
//        ContentValues columns_entry_list = new ContentValues();
//        columns_entry_list.put(DBHelper.observation_open, open);
//        columns_entry_list.put(DBHelper.observation_high, high);
//        columns_entry_list.put(DBHelper.observation_low, low);
//        columns_entry_list.put(DBHelper.observation_close, close);
//        columns_entry_list.put(DBHelper.observation_date, date);
//        columns_entry_list.put(DBHelper.stock_id, stock_id);
//        columns_entry_list.put(DBHelper.observation_volume, volume);
//
//
//        long insertId = database.insert(DBHelper.table_observation, null, columns_entry_list);
//
//        Cursor cursor = database.query(DBHelper.table_observation, columns_observation_list, DBHelper.observation_id + "=" + insertId,
//                null, null, null, null);
//
//        cursor.moveToFirst();
//        Equity.Observation observation = cursorToObservation(cursor);
//        cursor.close();
//
//        return observation;
//
//    }

    public Alert createPriceLimit(double upper, double lower, Equity eq){

        long id = eq.getId();
        ContentValues columns_entry_list = new ContentValues();
        columns_entry_list.put(DBHelper.stock_pricelimit, upper);
        columns_entry_list.put(DBHelper.stock_pricelimit1, lower);
        //columns_entry_list.put(DBHelper.stock_alert, false);
        columns_entry_list.put(DBHelper.stock_id, id);


        long insertId = database.insert(DBHelper.table_alert, null, columns_entry_list);

        Cursor cursor = database.query(DBHelper.table_alert, columns_alert_list, DBHelper.alert_id + "=" + insertId,
                null, null, null, null);

        cursor.moveToFirst();
        Alert alert = cursorToAlert(cursor);
        cursor.close();

        return alert;

    }

    private Alert cursorToAlert(Cursor cursor) {
        int id_index = cursor.getColumnIndex(DBHelper.alert_id);
        int id_upper = cursor.getColumnIndex(DBHelper.stock_pricelimit);
        int id_lower = cursor.getColumnIndex(DBHelper.stock_pricelimit1);
        //int id_alert = cursor.getColumnIndex(DBHelper.stock_alert);
        int id_stock = cursor.getColumnIndex(DBHelper.stock_id);


        long index = cursor.getLong(id_index);
        long upper = cursor.getInt(id_upper);
        long lower = cursor.getInt(id_lower);
        //Boolean alert = cursor.getBoolean(id_alert);
        long stock_id = cursor.getInt(id_stock);




        Alert alert = new Alert(index, upper, lower, stock_id);

        return alert;
    }

    private Equity cursorToEntry(Cursor cursor) {

        int id_index = cursor.getColumnIndex(DBHelper.stock_id);
        int id_symbol = cursor.getColumnIndex(DBHelper.stock_name);
        int id_price = cursor.getColumnIndex(DBHelper.stock_value);
        int id_date = cursor.getColumnIndex(DBHelper.stock_date);
        int id_timezone = cursor.getColumnIndex(DBHelper.stock_timezone);



        long index = cursor.getLong(id_index);
        String symbol = cursor.getString(id_symbol);
        double value = cursor.getDouble(id_price);

        String date = cursor.getString(id_date);
        String timezone = cursor.getString(id_timezone);




        Equity equity = new Equity(index,symbol,value, date, timezone );

        equity.setAbove(getUpperPrice(equity));
        equity.setUnder(getLowerPrice(equity));

        Cursor observationCursor = database.query(DBHelper.table_observation, columns_observation_list, DBHelper.stock_id + "=" + index,
                null, null, null, null);


        int id_open = observationCursor.getColumnIndex(DBHelper.observation_open);
        int id_high = observationCursor.getColumnIndex(DBHelper.observation_high);
        int id_low = observationCursor.getColumnIndex(DBHelper.observation_low);
        int id_close = observationCursor.getColumnIndex(DBHelper.observation_close);
        int id_datetime = observationCursor.getColumnIndex(DBHelper.observation_date);
        int id_volume = observationCursor.getColumnIndex(DBHelper.observation_volume);

        observationCursor.moveToFirst();

        while(!observationCursor.isAfterLast()){
            double open = observationCursor.getLong(id_open);
            double high = observationCursor.getLong(id_high);
            double low = observationCursor.getLong(id_low);
            double close = observationCursor.getLong(id_close);
            double volume= observationCursor.getLong(id_volume);
            String datetime  = observationCursor.getString(id_datetime);
            equity.addObservation(open,high,low,close,volume,datetime);
            observationCursor.moveToNext();
        }


        return equity;
    }

    public List<Equity> getAllEquities() {
        List<Equity> EntryList = new ArrayList<>();

        Cursor cursor = database.query(DBHelper.table_stock,
                columns_stock_list, null, null, null, null, null);

        cursor.moveToFirst();
        Equity equity;

        while(!cursor.isAfterLast()) {
            equity = cursorToEntry(cursor);
            EntryList.add(equity);
            cursor.moveToNext();
        }

        cursor.close();

        return EntryList;
    }

    public List<Alert> getAllAlerts() {
        List<Alert> AlertList = new ArrayList<>();

        Cursor cursor = database.query(DBHelper.table_alert,
                columns_alert_list, null, null, null, null, null);

        cursor.moveToFirst();
        Alert alert;

        while(!cursor.isAfterLast()) {
            alert = cursorToAlert(cursor);
            AlertList.add(alert);
            cursor.moveToNext();
        }

        cursor.close();

        return AlertList;
    }

    public void deleteEquity(Equity equity){
        long id = equity.getId();

        database.delete(DBHelper.table_stock,
                DBHelper.stock_id + "=" + id,
                null);
    }

    public void deleteAlert(Equity equity){
        long id = equity.getId();

        database.delete(DBHelper.table_alert,
                DBHelper.stock_id + "=" + id,
                null);
    }

    public void updateEquity(double value, Equity equity){
        ContentValues newValues = new ContentValues();
        long stock_id = equity.getId();
        newValues.put(DBHelper.stock_value, value);
        database.update(DBHelper.table_stock, newValues, DBHelper.stock_id+"="+stock_id, null);
    }

    public void updateAlert(double upper, double lower, Equity equity){
        ContentValues newValues = new ContentValues();
        long equity_id = equity.getId();
        newValues.put(DBHelper.stock_pricelimit, upper);
        newValues.put(DBHelper.stock_pricelimit1,lower );
        database.update(DBHelper.table_alert, newValues, DBHelper.stock_id+"="+equity_id, null);
    }

    public void DropEquities (){
        database.execSQL("DROP TABLE IF EXISTS '" + DBHelper.table_stock + "'");

    }

    public long getUpperPrice(Equity eq){
        long price = -1;
        long id = eq.getId();
        Cursor cursor = database.query(DBHelper.table_alert,
                columns_alert_list, null,//DBHelper.stock_id+"="+id,
                null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Alert alert = cursorToAlert(cursor);
            if(alert.stock_id==id) {
                price = alert.getUpper();
            }
            cursor.moveToNext();
        }
        cursor.close();
        return price;
    }

    public long getLowerPrice(Equity eq){
        long price = -1;
        long id = eq.getId();
        Cursor cursor = database.query(DBHelper.table_alert,
                columns_alert_list, DBHelper.stock_id+"="+id,
                null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            Alert alert = cursorToAlert(cursor);
            price = alert.getLower();
            cursor.moveToNext();
        }
        cursor.close();
        return price;
    }

    public boolean isEquityExcisting(Equity eq)
    {
        String symbol = eq.getSymbol();
        Cursor cursor = database.query(DBHelper.table_stock,
                columns_stock_list, DBHelper.stock_name+"="+symbol, null, null, null, null);
        if(cursor!=null){
            return true;
        }else{return false;}
    }

	
	
	
	
	
	
	
	
	
	
	
	
}