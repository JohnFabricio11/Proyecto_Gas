package ec.edu.tecnologicoloja.donled;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button btnOn, btnOff;
    TextView  sensorView0;
    Handler bluetoothIn;
    //utilizado para identificar el mensaje del controlador..
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;
    // Servicio SPP UUID: esto debería funcionar para la mayoría de los dispositivos..
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Cadena para la dirección MAC..
    private static String address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Vincular los botones y textViews a las respectivas vistas...
        btnOn = (Button) findViewById(R.id.buttonOn);
        btnOff = (Button) findViewById(R.id.buttonOff);
        sensorView0 = (TextView) findViewById(R.id.sensorView0);
        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        // Asignar acción si pulsamos on activamos gass...
        btnOn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("on");    // Envia "on" via Bluetooth
                btnOff.setBackgroundColor(Color.GRAY);
                btnOn.setBackgroundColor(Color.RED);
                Toast.makeText(getBaseContext(), "GAS ACTIVADO..", Toast.LENGTH_SHORT).show();
                sensorView0.setText("Gas encendido no olvides apagarlo");
            }
        });
         //boton para apagar...
        btnOff.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("off");    // Envia "off" via Bluetooth
                btnOn.setBackgroundColor(Color.GRAY);
                btnOff.setBackgroundColor(Color.GREEN);
                Toast.makeText(getBaseContext(), "GAS DESACTIVADO..", Toast.LENGTH_SHORT).show();
                sensorView0.setText("No esta pasando Gas");
            }
        });
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //crea una conexión saliente segura con un dispositivo BT usando UUID
    }
    @Override
    public void onResume() {
        super.onResume();

        //Obtenga la dirección MAC de DeviceListActivity a través de la intención
        Intent intent = getIntent();

        //Obtenga la dirección MAC de DeviceListActivty a través de EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //crear dispositivo y configurar la dirección MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establezca la conexión de la toma Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //inserte el código para tratar con esto
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //Envío un carácter al reanudar la transmisión inicial para comprobar que el dispositivo está conectado
        // Si no es una excepción, se lanzará en el método de escritura y se llamará a finish ()
        mConnectedThread.write("x");
    }
    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //No deje las tomas de Bluetooth abiertas cuando deje la actividad
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }
    //Comprueba que el Bluetooth del dispositivo Android esté disponible y solicita que se encienda si está apagado
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
    //crear una nueva clase para conectar el hilo
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creación del hilo de conexión
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Crea flujos  para la conexión
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            //Siga repitiendo para escuchar los mensajes recibidos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//leer bytes del búfer de entrada
                    String readMessage = new String(buffer, 0, bytes);
                    //Envíe los bytes obtenidos a la actividad de la interfaz de usuario a través del controlador
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //método de escritura
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //convierte la cadena ingresada en bytes
            try {
                mmOutStream.write(msgBuffer);                //escribir bytes sobre la conexión BT a través de la salida
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

}