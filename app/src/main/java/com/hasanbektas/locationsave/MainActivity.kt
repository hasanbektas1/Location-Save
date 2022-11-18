package com.hasanbektas.locationsave

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.hasanbektas.locationsave.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    private var compositeDisposable = CompositeDisposable()


    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        val db = Room.databaseBuilder(applicationContext,LocationDatabase::class.java,"Places").build()
        val placeDao = db.locationDao()

        compositeDisposable.add(
            placeDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }
    private fun handleResponse(placeList : List<Location>){

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = LocationAdapter(placeList)
        binding.recyclerView.adapter = adapter

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.location_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add_place){

            val intent = Intent(this,MapsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}