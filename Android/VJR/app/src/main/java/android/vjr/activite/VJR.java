package android.vjr.activite;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.vjr.Bluetooth;
import android.vjr.R;
import android.widget.TextView;
import android.vjr.View.JoystickView;
import android.widget.Toast;

public class VJR extends Activity  {

    private TextView angleTextView;         // label permettant d'afficher l'angle
    private TextView powerTextView;         // label permettant d'afficher le % de puissance
    private TextView directionTextView;     // label donnant la direction
    private TextView textViewModeDegree;

    private JoystickView joystick;          // Import de la vue joystick
    private JoystickView joystick2;

    private Bluetooth bt = null;
    private long lastTime = 0;
    private float[] acceleromterVector = new float[3];
    private double lastAngle = 0;
    private int lastPower = 0;
    private double angleSensor = 0;
    private String BTAddr = "";


    private double globalAngle = 0;
    private int globalPower = 0;

    //private boolean MODE_0_TO_180 = false;




    final Handler handler = new Handler() { // au cas ou l'on ait besoin de recevoir j'implémente un handler
        public void handleMessage(Message msg) {
            String data = msg.getData().getString("receivedData");
            long t = System.currentTimeMillis();
            if (t - lastTime > 100) {          // Pour éviter que les messages soit coupés
                Log.d(" ", "\n");
                lastTime = System.currentTimeMillis();
            }
            Log.d("data : ", data);
        }
    };
    final Handler handlerStatus = new Handler() {
        public void handleMessage(Message msg) {
            int co = msg.arg1;
            if (co == 1) {
                // implémenter un texte ou une icone montrant que c'est connecté (ou pas)
                Log.d("status :", "Connected\n");
                Toast.makeText(getApplicationContext(),"Connected",Toast.LENGTH_LONG).show();
            } else if (co == 2) {
                Log.d("status :", "Disconnected\n");
                Toast.makeText(getApplicationContext(),"Disconnected",Toast.LENGTH_LONG).show();

            }
        }
    }; // Handler permettant de définir le statut de connecté ou déconnecté

    /**
     * *************************************************
     * Called when the activity is first created.      *
     * **************************************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vjr);
        // référencement des composants UI
        angleTextView = (TextView) findViewById(R.id.angleTextView);
        powerTextView = (TextView) findViewById(R.id.powerTextView);
        directionTextView = (TextView) findViewById(R.id.directionTextView);
                //textViewModeDegree = (TextView) findViewById(R.id.textViewModeDegree);
        ///textViewModeDegree.setText(String.valueOf(MODE_0_TO_180));


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            BTAddr = extras.getString("BTAddr");
            // and get whatever type user account id is
            Log.d("HC06 addrMAc", BTAddr);
            bt = new Bluetooth(handlerStatus, handler, BTAddr);
        }


        new Thread(new Runnable() {
            public void run() {


                try {
                    while(true) {
                        Thread.sleep(10);

                            if (globalAngle != lastAngle || globalPower != lastPower) {

                            if((globalAngle<= -90 && globalAngle>= -180) || (globalAngle >= 90 && globalAngle <=180)) {
                                globalAngle = globalAngle * -1;
                            }


                            Log.d("DATAS sent to " + String.valueOf(lastAngle) + " : ", String.valueOf(lastPower));
                            bt.sendData(String.valueOf(lastAngle) + ';' + String.valueOf(lastPower) + '\n');
                            lastAngle = globalAngle;
                            lastPower = globalPower;

                            }

                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
           }

        }).start();


        //Référencement de la vue
        joystick = (JoystickView) findViewById(R.id.joystick);
        joystick2 = (JoystickView) findViewById(R.id.joystick2);
        joystick2.joystickMode = 1;


        // Écouteur d'événement retourne :
        // l'angle valeur possible -179° a 180°
        // la puissance 0 - 100%
        // la direction
        joystick.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub




                    angleTextView.setText(" " + String.valueOf(angle) + "°");
                    globalAngle = angle;


                switch (direction) {
                    case JoystickView.FRONT:
                        directionTextView.setText(R.string.front_lab);
                        break;
                    case JoystickView.FRONT_RIGHT:
                        directionTextView.setText(R.string.front_right_lab);
                        break;
                    case JoystickView.RIGHT:
                        directionTextView.setText(R.string.right_lab);
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        directionTextView.setText(R.string.right_bottom_lab);
                        break;
                    case JoystickView.BOTTOM:
                        directionTextView.setText(R.string.bottom_lab);
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        directionTextView.setText(R.string.bottom_left_lab);
                        break;
                    case JoystickView.LEFT:
                        directionTextView.setText(R.string.left_lab);
                        break;
                    case JoystickView.LEFT_FRONT:
                        directionTextView.setText(R.string.left_front_lab);
                        break;
                    default:
                        directionTextView.setText(R.string.center_lab);
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);// toutes les 100 ms actualisation des données


        joystick2.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                // TODO Auto-generated method stub


                    powerTextView.setText(" " + String.valueOf(power) + "%");
                    globalPower = power;
                //    Log.d("Device not connected : ", String.valueOf(angle) + "\n");


                switch (direction) {
                    case JoystickView.FRONT:
                        directionTextView.setText(R.string.front_lab);
                        break;
                    case JoystickView.FRONT_RIGHT:
                        directionTextView.setText(R.string.front_right_lab);
                        break;
                    case JoystickView.RIGHT:
                        directionTextView.setText(R.string.right_lab);
                        break;
                    case JoystickView.RIGHT_BOTTOM:
                        directionTextView.setText(R.string.right_bottom_lab);
                        break;
                    case JoystickView.BOTTOM:
                        directionTextView.setText(R.string.bottom_lab);
                        break;
                    case JoystickView.BOTTOM_LEFT:
                        directionTextView.setText(R.string.bottom_left_lab);
                        break;
                    case JoystickView.LEFT:
                        directionTextView.setText(R.string.left_lab);
                        break;
                    case JoystickView.LEFT_FRONT:
                        directionTextView.setText(R.string.left_front_lab);
                        break;
                    default:
                        directionTextView.setText(R.string.center_lab);
                }
            }
        }

                , JoystickView.DEFAULT_LOOP_INTERVAL);// toutes les 100 ms actualisation des données

    }


    @Override
    protected void onPause() {
// désenregistrer tous le monde
        super.onPause();
        Toast.makeText(getApplicationContext(),"Paused",Toast.LENGTH_LONG).show();
        bt.close();
     finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_vjr, menu);
        return true;
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("BTAddr", BTAddr);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        BTAddr = savedInstanceState.getString("BTAddr");
        bt = new Bluetooth(handlerStatus, handler, BTAddr);
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_mod) {
            if(joystick.MOD_DEFAULT == JoystickView.DEFAULT_STAY_POS)
            {
                joystick.setModDefault(JoystickView.DEFAULT_BACK_MIDDLE);
                joystick2.setModDefault(JoystickView.DEFAULT_BACK_MIDDLE);
            }else {
                joystick.setModDefault(JoystickView.DEFAULT_STAY_POS);
                joystick2.setModDefault(JoystickView.DEFAULT_STAY_POS);

            }
        }

        if (id == R.id.action_settings) {

            return true;
        }
        if(id == R.id.action_about) {
            Toast.makeText(this.getApplicationContext(), "Application créee par : \n" +
                    "Bertrand Lelann (bertrand.lelann@viacesi.fr) \n" +
                    "Kevin Rosala (kevin.rosala@viacesi.fr) \n" +
                    "Ambroise Daventure (ambroise.daventure@viacesi.fr) \n" +
                    "Pierre Cointe (pierre.cointe@viacesi.fr) \n" +
                    "Tom Andrivet (tom.andrivet@viacesi.fr) \n", Toast.LENGTH_LONG).show();

            return true;
        }
        if (id == R.id.action_deco) {
            bt.close();
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 90:
                if (resultCode == RESULT_OK) {
                    Bundle res = data.getExtras();
                    String addr = res.getString("BTAddr");
                    BTAddr = addr;
                    Log.d("FIRST", "addr: " + addr);
                    // création de la classe bluetooth
                    bt = new Bluetooth(handlerStatus, handler, addr);




                }
                break;
        }
    }
}