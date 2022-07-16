package edu.msudenver.bucketlist

/*
 * CS3013 - Mobile App Dev. - Summer 2022
 * Instructor: Thyago Mota
 * Student(s): Brandon Young
 * Description: App 02 - MainActivity (controller) class
 */

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    lateinit var recyclerView: RecyclerView
    lateinit var dbHelper: DBHelper

    // TODO #1: create the ItemHolder inner class
    // a holder object saves the references to view components of a recycler view item
    private inner class ItemHolder(view: View): RecyclerView.ViewHolder(view) {

        val txtId:TextView = view.findViewById(R.id.txtId)//int
        val txtDescription:TextView = view.findViewById(R.id.txtDescription)//string
        val txtCreationDate:TextView = view.findViewById(R.id.txtCreationDate)//date
        val txtUpdateDate:TextView = view.findViewById(R.id.txtUpdateDate)//date
        val txtStatus:TextView = view.findViewById(R.id.txtStatus)//int
    }

    // TODO #2: create the ItemAdapter inner class
    // an item adapter binds items from a list to holder objects in a recycler view
    private inner class ItemAdapter(var bucketlist: List<Item>, var onClickListener: View.OnClickListener, var onLongClickListener: View.OnLongClickListener): RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.bucket_list, parent, false)

            return ItemHolder(view)
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {

            val item = bucketlist[position]
            holder.txtId.text = (item.id.toString().toInt() + 1).toString()
            holder.txtDescription.text = "Description: " + item.description
            holder.txtCreationDate.text = "Date created: "+ DBHelper.ISO_FORMAT.format(item.creationDate).toString()
            holder.txtUpdateDate.text = "Date updated: " + DBHelper.ISO_FORMAT.format(item.updateDate).toString()
            holder.txtStatus.text = Item.STATUS_DESCRIPTIONS[item.status]

        }

        override fun getItemCount(): Int {
            return bucketlist.size
        }
    }

    // TODO #3: populate the recycler view
    // this function should query the database for all of the bucket list items; then use the list to update the recycler view's adapter
    // don't forget to call "sort()" on your list so the items are displayed in the correct order
    fun populateRecyclerView() {
        val db = dbHelper.readableDatabase
        val items = mutableListOf<Item>()

        db.execSQL(
            """
            DROP TABLE IF EXISTS bucketList
        """)
        dbHelper.onCreate(db)
        // The list would stay the same if not for these

        val cursor = db.query(
            "bucketlist",
            null,
            null,
            null,
            null,
            null,
            null
        )
        with (cursor) {
            while (moveToNext()) {
                val id = position
                val description    = getString(0)
                val createdDate = DBHelper.ISO_FORMAT.parse(getString(1))
                val updatedDate = DBHelper.ISO_FORMAT.parse(getString(2))
                val spinnerState     = getInt(3)
                val item = Item(id, description, createdDate, updatedDate, spinnerState)
                items.add(item)
            }
        }
        recyclerView.adapter = ItemAdapter(items, this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO #4: create and populate the recycler view
        dbHelper = DBHelper(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)



        populateRecyclerView()


        // TODO #5: initialize the floating action button
        val fltBtnCreate : FloatingActionButton = findViewById(R.id.fltBtnCreate)
        fltBtnCreate.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        populateRecyclerView()
    }

    // TODO #6: call CreateUpdateActivity for update
    // don't forget to pass the item's id to the CreateUpdateActivity via the intent
    override fun onClick(view: View?) {
        if (view != null) {

            val id = view.findViewById<TextView>(R.id.txtId).toString().toInt()
            val intent = Intent(this, CreateUpdateActivity::class.java)
            intent.putExtra("op", CreateUpdateActivity.CREATE_OP)
            intent.putExtra("id",id)
            startActivity(intent)
        }
    }

    // TODO #7: delete the long tapped item after a yes/no confirmation dialog
    override fun onLongClick(view: View?): Boolean {

        class MyDialogInterfaceListener(val id: Int): DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    try {
                        val db = dbHelper.writableDatabase
                        db.execSQL(
                            """
                                DELETE FROM bucketlist
                                WHERE id = "${id}"
                            """
                        )

                    } catch (ex: Exception) {

                    }
                }
            }
        }

        if (view != null) {
            val id = view.findViewById<TextView>(R.id.txtId).toString().toInt()
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Do you wish to delete this entry?")
            alertDialogBuilder.setPositiveButton("Yes", MyDialogInterfaceListener(id))
            alertDialogBuilder.setNegativeButton("No", MyDialogInterfaceListener(id))
            alertDialogBuilder.show()

            return true
        }
        return false
    }
}