package ec.edu.tecnologicoloja.donled;

import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class DeviceListActivity extends Activity {
    // Depuración de LOGCAT
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;
    
  
    // Botón de declaración para iniciar el sitio web y vista de texto para el estado de la conexión
    Button tlbutton;
    TextView textView1;
    
    // Cadena EXTRA para enviar a la actividad principal
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Campos de miembros
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_list);
    }
    
    @Override
    public void onResume() 
    {
    	super.onResume();
    	//*************** 
    	checkBTState();

    	textView1 = (TextView) findViewById(R.id.connecting);
    	textView1.setTextSize(40);
    	textView1.setText(" ");

    	// Inicializar el adaptador de matriz para dispositivos emparejados
    	mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

    	// Busque y configure ListView para dispositivos emparejados
    	ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
    	pairedListView.setAdapter(mPairedDevicesArrayAdapter);
    	pairedListView.setOnItemClickListener(mDeviceClickListener);

    	// Obtenga el adaptador Bluetooth local
    	mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    	// Get a set of currently paired devices and append to 'pairedDevices'
    	Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

    	// Agregue dispositivos previamente emparejados a la matriz
    	if (pairedDevices.size() > 0) {
    		findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
    		for (BluetoothDevice device : pairedDevices) {
    			mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    		}
    	} else {
    		String noDevices = getResources().getText(R.string.none_paired).toString();
    		mPairedDevicesArrayAdapter.add(noDevices);
    	}
  }

    // Configure el oyente al hacer clic para la lista (apodado esto - no estoy seguro)
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

        	textView1.setText("CARGANDO...");
            // Obtenga la dirección MAC del dispositivo, que son los últimos 17 caracteres en la Vista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Intente comenzar la siguiente actividad mientras toma un extra, que es la dirección MAC.
			Intent i = new Intent(DeviceListActivity.this, MainActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
			startActivity(i);   
        }
    };

    private void checkBTState() {
        // Verifique que el dispositivo tenga Bluetooth y que esté encendido
    	 mBtAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) { 
        	Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
          if (mBtAdapter.isEnabled()) {
            Log.d(TAG, "...Bluetooth Activado...");
          } else {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
 
            }
          }
        }
}