package com.ituran.mapsapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson

class MapsActivity : AppCompatActivity(), OnMapReadyCallback , GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener{



    private val permisoFineLocation=android.Manifest.permission.ACCESS_FINE_LOCATION
    private val permisoCoarseLocation=android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val CODIGO_SOLICITUD_PERMISO=100

    private var fusedLocationClient : FusedLocationProviderClient ?= null

    //Variable para estar midiendo el tiempo de respuesta para estar pidiendo ubicacion
    private var locationRequest:LocationRequest?=null

    //Variable coolbac para dentener actualizacion de hubicacion
    private var callback:LocationCallback?=null

    //variable para colocar marcadores de usuario
    private var listaMarcadores: ArrayList<Marker>? = null

    //Variable para obtener la posiscion de forma local
    private var miPosicion:LatLng? = null


    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //Obtener the suppoortMapFragment and get notified when the map is ready
        val mapFragment=supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient=FusedLocationProviderClient(this)
        inicializarLocationRequest()



        callback=object:LocationCallback(){

            override fun onLocationResult(locationResult : LocationResult?){
                super.onLocationResult(locationResult)


                if(mMap!=null){
                    //boton para obtener la ubicacion
                    mMap.isMyLocationEnabled=true
                    mMap.uiSettings.isMyLocationButtonEnabled=true

                    for(ubicacion in locationResult?.locations!!){
                        Toast.makeText(applicationContext, ubicacion.latitude.toString() + "," +ubicacion.longitude.toString(),Toast.LENGTH_LONG).show()

                        //Add a marker in Sydney and move the camera
                        miPosicion=LatLng(ubicacion.latitude, ubicacion.longitude)
                        mMap.addMarker(MarkerOptions().position(miPosicion!!).title("Aqui estoy"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(miPosicion))
                    }
                }
            }
        }
    }




    //inicializarLocationRequest...GOOGLE
    private fun inicializarLocationRequest(){
        locationRequest= locationRequest
        locationRequest?.interval=10000
        locationRequest?.fastestInterval=5000
        //proximidad..metros
        locationRequest?.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    //VALIDAR PERMISOS
    private fun validarPermisosUbicacion():Boolean{
        val hayUbicacionPrecisa=ActivityCompat.checkSelfPermission(this,permisoFineLocation)==PackageManager.PERMISSION_GRANTED
        val hayUbicacionOrdinaria=ActivityCompat.checkSelfPermission(this, permisoCoarseLocation)==PackageManager.PERMISSION_GRANTED

        return hayUbicacionPrecisa && hayUbicacionOrdinaria
    }





    override fun onMapReady(googleMap: GoogleMap) {
        mMap=googleMap

        //tipos de mapa
        //mMap.mapType=GoogleMap.Map_TYPE_HYBRID

        crearListeners()



        //funcion para preparar los marcadores
        prepararMarcadores()

        //Comenzamos a dibujar las lineas
        dibujarLineas()
    }



    private fun dibujarLineas() {

        val coordenadasLineas=PolylineOptions()
            .add(LatLng(19.43420011141154, -99.147705696582))
            .add(LatLng(19.43420011141154, -99.147705696582))
            .add(LatLng(19.43420011141154, -99.147705696582))
            .add(LatLng(19.43420011141154, -99.147705696582))


       /* val coordenadas=PolygonOptions()
            .add(LatLng(19.43420011141154, -99.147705696582))
            .add(LatLng(19.43420011141154, -99.147705696582))
            .add(LatLng(19.43420011141154, -99.147705696582))
            .add(LatLng(19.43420011141154, -99.147705696582))
        */


       // mMap.addPolyline(coordenadasLineas)
       // mMap.addPolygon(coordenadas)
    }

    private fun prepararMarcadores() {

        listaMarcadores=ArrayList()

        //se ocupa onLongClickListener porque es lo mejor para los mapas ya que pueden tener diferentes tipos de listener
        mMap.setOnMapLongClickListener {
                location:LatLng? ->

            listaMarcadores?.add(mMap.addMarker(MarkerOptions()
                .position(location!!)
                .snippet("TU TEXTO EXTRA PARA A—ADIR INFO A EL MARCADOR")
                .alpha(0.7f)//opacidad del icono
                .title("GOLDEN GATE"))
            )


            //para mover los marcadores que aÒadimos con un click
            listaMarcadores?.last()!!.isDraggable=true

            //coordenadas va a obtener el ultimo marcador
            val cooredenadas=LatLng(listaMarcadores?.last()!!.position.latitude, listaMarcadores?.last()!!.position.longitude)

            //se manda allamar la URL para hacer la uniion entre las dos corendadas

            val origen = "origin"+ miPosicion?.latitude + "," + miPosicion?.longitude + "&"
            val destino = "destination="+ cooredenadas.latitude + "," + cooredenadas.longitude + "&"
            val parametros = origen + destino +"sensor=false&mode=driving"

            cargarURL("http//maps.googleapis.com/maps/api/directions/json?" +parametros )

        }

    }



    private fun crearListeners() {
    //metodo que hace referencia a los marcadores
        mMap.setOnMarkerClickListener(this)
        //metodo para aÒadir ubicacion a un nuevo marcador
        //para esto aÒadimos extends implemts...GoogleMap.OnMarkerDragListener
        mMap.setOnMarkerDragListener(this)
    }



    override fun onMarkerClick(p0: Marker?): Boolean {
        var numerodeClick = p0?.tag as? Int

                if(numerodeClick!= null){
                    numerodeClick++
                    p0?.tag=numerodeClick
                    Toast.makeText(this, "se han dado" +numerodeClick.toString()+ "clciks", Toast.LENGTH_SHORT).show()
                }

        //return false para que detecte que vamso a sobre escribbir el  metodo
        return false

    }

    override fun onMarkerDragEnd(p0: Marker?) {

        Toast.makeText(this,"se termino de mover",Toast.LENGTH_SHORT).show()
        Log.d("MARCADOR FINAL", p0?.position?.latitude.toString())

    }

    override fun onMarkerDragStart(p0: Marker?) {
        Toast.makeText(this,"estas movuiendo el marcador",Toast.LENGTH_SHORT).show()
        //variable que nos da el marcador
        Log.d("MARCADOR INICIAL", p0?.position?.latitude.toString())
        val index =listaMarcadores?.indexOf(p0!!)
        //me da la latitud del elemento
        Log.d("MARCADOR INICIAL", listaMarcadores?.get(index!!)!!.position?.latitude.toString())

    }

    override fun onMarkerDrag(p0: Marker?) {
        title =p0?.position?.latitude.toString() + "-" +p0?.position?.longitude.toString()

    }


    //Obtener la ubicacion que se haya utilizado anteriormente
    @SuppressLint("MissingPermission")
    private fun obtenerUbicacion(){
        fusedLocationClient?.requestLocationUpdates(locationRequest, callback,null)
    }



    private fun pedirPermisos(){
        val deboProveerContexto=ActivityCompat.shouldShowRequestPermissionRationale(this,permisoFineLocation)

        if(deboProveerContexto){
            //Pedir permisos adicionales
            Toast.makeText(this,"Necesitamos que nos des permisos de ubiacion",Toast.LENGTH_LONG).show()
            solicitudPermiso()
        }else{
            solicitudPermiso()
        }
    }

    //ya se pidieron permisos
    private fun solicitudPermiso(){
        requestPermissions(arrayOf(permisoFineLocation,permisoCoarseLocation), CODIGO_SOLICITUD_PERMISO )
    }


    //acompeltar con shift mas espacio
     fun onRequestPermissionResult(requeestCode:Int, permissions:Array<out String>, grantResults:IntArray){
        super.onRequestPermissionsResult(requeestCode,permissions,grantResults)

        when(requeestCode){
            CODIGO_SOLICITUD_PERMISO ->{
                if(grantResults.size > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //obtener ubicacion
                    obtenerUbicacion()

                }else{
                    Toast.makeText(this,"No diste permisos para acceder a la ubicacion",Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun detenerActualizaconUbicacion(){
        fusedLocationClient?.removeLocationUpdates(callback)
    }

    private fun cargarURL(url:String){
        val queue =Volley.newRequestQueue(this)

        val solicitud =StringRequest(Request.Method.GET, url, Response.Listener<String>{

                response ->
            Log.d("HTTP",response)

            //Una vez teniendo la clase qmodel oque va a obtener la respuesta de api google
           val coordenasas = obtenerCoordenadas(response)
            //ya teniendo las coordenadas se llama a Mapa
            mMap.addPolyline(coordenasas)



        }, Response.ErrorListener{})

        //aÒadir al quequue la solicitud
        queue.add(solicitud)
    }



    //se mapea la respuesta JSON
    private fun obtenerCoordenadas(json:String):PolylineOptions{
        val gson =Gson()
        val objeto = gson.fromJson(json, com.ituran.mapsapplication.Response::class.java)
        val puntos =objeto.routes?.get(0)!!.legs?.get(0)!!.steps!!
        var coordenadas=PolylineOptions()

        for(punto in puntos){
            //coordenadas.add(LatLng(punto.start_location))
            coordenadas.add(punto.start_location?.toLatLng())
            coordenadas.add(punto.end_location?.toLatLng())
        }

        coordenadas.color(Color.BLACK)
            .width(15f)
        return coordenadas
    }


    override fun onStart(){
        super.onStart()

        if(validarPermisosUbicacion()){
            obtenerUbicacion()
            }else{
                pedirPermisos()
            }
        }


        override fun onPause(){
            super.onPause()
            detenerActualizaconUbicacion()
        }


    }



