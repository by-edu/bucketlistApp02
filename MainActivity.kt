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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    lateinit var recyclerView: RecyclerView
    lateinit var dbHelper: DBHelper
    //var id : Int = 0

    // TODOd #1: create the ItemHolder inner class
    // a holder object saves the references to view components of a recycler view item
    private inner class ItemHolder(view: View): RecyclerView.ViewHolder(view) {

        val txtId:TextView = view.findViewById(R.id.txtId)//int
        val txtDescription:TextView = view.findViewById(R.id.txtDescription)//string
        val txtCreationDate:TextView = view.findViewById(R.id.txtCreationDate)//date
        val txtUpdateDate:TextView = view.findViewById(R.id.txtUpdateDate)//date
        val txtStatus: ImageView = view.findViewById(R.id.txtStatus)//int
    }

    // TODOd #2: create the ItemAdapter inner class
    // an item adapter binds items from a list to holder objects in a recycler view
    private inner class ItemAdapter(var bucketlist: List<Item>, var onClickListener: View.OnClickListener, var onLongClickListener: View.OnLongClickListener): RecyclerView.Adapter<ItemHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.bucket_list, parent, false)

            return ItemHolder(view, )
        }

        override fun onBindViewHolder(holder: ItemHolder, position: Int) {

            val item = bucketlist[position]
            holder.txtId.text = (item.id.toString().toInt()).toString()
            holder.txtDescription.text = "Description: " + item.description
            holder.txtCreationDate.text = "Date created: "+ DBHelper.USA_FORMAT.format(item.creationDate).toString()
            holder.txtUpdateDate.text = "Date updated: " + DBHelper.USA_FORMAT.format(item.updateDate).toString()
            //holder.txtStatus.setImageResource(R.drawable.scheduled_item)

            if (item.status == 0){
                holder.txtStatus.setImageResource(R.drawable.scheduled_item)

            } else if (item.status == 1) {
                holder.txtStatus.setImageResource(R.drawable.completed_item)

            } else if (item.status == 2){
                holder.txtStatus.setImageResource(R.drawable.archived_item)

            }

            holder.itemView.setOnClickListener(onClickListener)
            holder.itemView.setOnLongClickListener(onLongClickListener)

        }

        override fun getItemCount(): Int {
            return bucketlist.size
        }
    }

    // TODOd #3: populate the recycler view
    // this function should query the database for all of the bucket list items; then use the list to update the recycler view's adapter
    // don't forget to call "sort()" on your list so the items are displayed in the correct order
    fun populateRecyclerView() {
        val db = dbHelper.readableDatabase
        val items = mutableListOf<Item>()

        /*db.execSQL(
            """
            DROP TABLE IF EXISTS bucketList
        """)
        dbHelper.onCreate(db)*/
        // The list would stay the same if not for these

        val cursor = db.query(
            "bucketlist",
            arrayOf("rowid, description, creation_date, update_date, status"),
            null,
            null,
            null,
            null,
            null
        )
        with (cursor) {
            while (moveToNext()) {


                val id = getInt(0)
                val description    = getString(1)
                val createdDate = DBHelper.ISO_FORMAT.parse(getString(2))
                val updatedDate = DBHelper.ISO_FORMAT.parse(getString(3))
                val spinnerState     = getInt(4)
                val item = Item(id, description, createdDate, updatedDate, spinnerState)
                items.add(item)
            }
        }
        recyclerView.adapter = ItemAdapter(items, this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODOd #4: create and populate the recycler view
        dbHelper = DBHelper(this)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        populateRecyclerView()


        // TODOd #5: initialize the floating action button
        val fltBtnCreate : FloatingActionButton = findViewById(R.id.fltBtnCreate)
        fltBtnCreate.setOnClickListener{

            val intent = Intent(this, CreateUpdateActivity::class.java)
            intent.putExtra("op", CreateUpdateActivity.CREATE_OP)
            startActivity(intent)
        }

    }

    override fun onResume() {
        super.onResume()
        populateRecyclerView()
    }

    // TODOd #6: call CreateUpdateActivity for update
    // don't forget to pass the item's id to the CreateUpdateActivity via the intent
    override fun onClick(view: View?) {
        if (view != null) {

            val textShow = view.findViewById<TextView>(R.id.txtId)
            val id = textShow.getText().toString().toInt()
            val intent = Intent(this, CreateUpdateActivity::class.java)
            intent.putExtra("op", CreateUpdateActivity.UPDATE_OP)
            intent.putExtra("id", id)
            startActivity(intent)
        }
    }

    // TODOd #7: delete the long tapped item after a yes/no confirmation dialog
    override fun onLongClick(view: View?): Boolean {

        class MyDialogInterfaceListener(val id: Int): DialogInterface.OnClickListener {
            override fun onClick(dialogInterface: DialogInterface?, which: Int) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    try {
                        val db = dbHelper.writableDatabase
                        db.execSQL(
                            """
                                DELETE FROM bucketlist
                                WHERE rowid = "${id}"
                            """
                        )
                        onResume()

                    } catch (ex: Exception) {

                    }
                }
            }
        }

        if (view != null) {

            val textShow = view.findViewById<TextView>(R.id.txtId)
            val id = textShow.getText().toString().toInt()
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