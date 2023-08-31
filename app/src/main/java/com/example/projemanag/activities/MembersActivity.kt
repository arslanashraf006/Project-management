package com.example.projemanag.activities

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.MemberListItemsAdapter
import com.example.projemanag.firebase.FirestoreClass
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utilis.Constants
import kotlinx.android.synthetic.main.activity_members.*
import kotlinx.android.synthetic.main.activity_task_list.*
import kotlinx.android.synthetic.main.dialog_search_member.*

class MembersActivity : BaseActivity() {
    private lateinit var mBoardDetails : Board
    private lateinit var mAssignedMembersList:ArrayList<User>
    // A global variable for notifying any changes done or not in the assigned members list.
    private var anyChangesDone: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members)

        if (intent.hasExtra(Constants.BOARD_DETAIL)){
            mBoardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }
        setupActionBar()
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getAssignedMembersListDetails(
            this@MembersActivity,
            mBoardDetails.assignedTo
        )
    }

    // Send the result to the base activity onBackPressed.
    override fun onBackPressed() {
        if (anyChangesDone) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu to use in the action bar
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.action_add_member -> {

                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    fun setupMembersList(list: ArrayList<User>) {

        mAssignedMembersList = list

        hideProgressDialog()

        rv_members_list.layoutManager = LinearLayoutManager(this@MembersActivity)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this@MembersActivity, list)
        rv_members_list.adapter = adapter
    }

    private fun  setupActionBar(){
        setSupportActionBar(toolbar_members_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.members)
        }

        toolbar_members_activity.setNavigationOnClickListener { onBackPressed() }
    }


    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        /*Set the screen content from a layout resource.
    The resource will be inflated, adding all top-level views to the screen.*/
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener(View.OnClickListener {

            val email = dialog.et_email_search_member.text.toString()

            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                FirestoreClass().getMemberDetails(this@MembersActivity, email)
            } else {
                Toast.makeText(
                    this@MembersActivity,
                    "Please enter members email address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        dialog.tv_cancel.setOnClickListener(View.OnClickListener {
            dialog.dismiss()
        })
        //Start the dialog and display it on screen.
        dialog.show()
    }

    fun memberDetails(user: User) {

        mBoardDetails.assignedTo.add(user.id)

        FirestoreClass().assignMemberToBoard(this@MembersActivity, mBoardDetails, user)
    }

    fun memberAssignSuccess(user: User) {

        hideProgressDialog()

        mAssignedMembersList.add(user)

        // Here the list is updated so change the global variable which we have declared for notifying changes.
        anyChangesDone = true

        setupMembersList(mAssignedMembersList)
    }
}