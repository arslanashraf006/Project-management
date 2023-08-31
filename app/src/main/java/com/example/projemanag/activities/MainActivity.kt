package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardItemsAdapter
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utilis.Constants
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var binding : ActivityMainBinding? = null
    private lateinit var mSharedPreferences: SharedPreferences

    companion object {
        const val MY_PROFILE_REQUEST_CODE : Int = 11
        const val CREATE_BOARD_REQUEST_CODE : Int = 12
    }

    private lateinit var mUserName : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        setupActionBar()
        binding!!.navView.setNavigationItemSelectedListener(this)

        mSharedPreferences = this.getSharedPreferences(
            Constants.PROJEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        FirestoreClass().loadUserData(this,  true)

        fab_create_board.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUserName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }

    }

    fun populateBoardsListToUI(boardList : ArrayList<Board>){
        hideProgressDialog()

        if (boardList.size > 0){
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility =  View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(
                object : BoardItemsAdapter.OnClickListener{
                    override fun onClick(position: Int, model: Board) {
                        val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                        intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                        startActivity(intent)
                    }
                }
            )
        }else{
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility =  View.VISIBLE
        }
    }

    @SuppressLint("ResourceType")
    private fun  setupActionBar(){
       setSupportActionBar(toolbar_main_activity)
        toolbar_main_activity.setNavigationIcon(R.drawable.ic_action_navigation_menu)

        toolbar_main_activity.setNavigationOnClickListener {
                toggleDrawer()
            }
    }

    private fun toggleDrawer(){
        if (binding!!.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            binding!!.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {
        if (binding!!.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            doubleBackToExit()
        }
    }
    fun updateNavigationUserDetails(user : User, readBoardList : Boolean){

        mUserName = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(nav_user_image)

        tv_username.text = user.name

        if (readBoardList){
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == MY_PROFILE_REQUEST_CODE){
            FirestoreClass().loadUserData(this)
        }else if (resultCode == Activity.RESULT_OK
            && requestCode == CREATE_BOARD_REQUEST_CODE){
            FirestoreClass().getBoardsList(this)
        }
        else{
            Log.e("Cancelled", "Cancelled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                startActivityForResult(Intent(this, MyProfileActivity::class.java), MY_PROFILE_REQUEST_CODE)
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    fun tokenUpdateSuccess(){

    }
}