package com.example.gustiehub

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class MarketplaceActivity: AppCompatActivity(){
    private lateinit var menuRecyclerView: RecyclerView
    private lateinit var menuAdapter: MenuAdapter
    private val groupList = mutableListOf<Group>()
    private val filteredGroupList = mutableListOf<Group>()
    private lateinit var marketplaceAdapter: MarketplaceAdapter
    private val itemList = mutableListOf<Marketplace>()

    // variables for toolbar and tabbed navigation
    lateinit var navView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    private lateinit var getContent: ActivityResultLauncher<String>
    private var selectedPhotoUri: Uri? = null

    val db = FirebaseFirestore.getInstance()

    // variables for searchbar
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
//    private lateinit var marketplaceAdapter: MarketplaceAdapter
//    private lateinit var marketplaceList: MutableList<MarketplaceItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_marketplace)

        val marketplaceRecyclerView = findViewById<RecyclerView>(R.id.marketplaceRecyclerView)
        marketplaceRecyclerView.layoutManager = LinearLayoutManager(this)
        marketplaceAdapter = MarketplaceAdapter(itemList)
        marketplaceRecyclerView.adapter = marketplaceAdapter

        getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            selectedPhotoUri = uri
        }
        listenForItemsUpdate()

        // list of groups in tab
        val userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        menuRecyclerView = findViewById(R.id.recycler_menu)
        menuRecyclerView.layoutManager = LinearLayoutManager(this)
        menuAdapter = MenuAdapter(filteredGroupList) { selectedGroup ->
            val intent = Intent(this, GroupPageActivity::class.java)
            intent.putExtra("groupName", selectedGroup.name)
            startActivity(intent)
        }
        menuRecyclerView.adapter = menuAdapter
        GlobalData.getFilteredGroupList(userID){ updatedGroups ->
            runOnUiThread {
                groupList.clear()
                groupList.addAll(updatedGroups)
                menuAdapter.updateGroups(updatedGroups)
            }
        }

        //set up drawer layout and handle clicks for menu items
        navView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.tab_layout)
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    startActivity(intent)
                }
                R.id.announcements -> {
                    val intent = Intent(this, AnnouncementsActivity::class.java)
                    startActivity(intent)
                }
                R.id.marketplace -> {
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
                R.id.events -> {
                    val intent = Intent(this, EventsActivity::class.java)
                    startActivity(intent)
                }
                R.id.groups -> {
                    val intent = Intent(this, GroupsActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // opening menu
        val menuButton: ImageView = findViewById(R.id.menu)
        menuButton.setOnClickListener {
            val drawerLayout = findViewById<DrawerLayout>(R.id.tab_layout)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // initialize and handle clicks for message and profile buttons
        val messageButton: ImageView = findViewById(R.id.messaging)
        val profileButton: ImageView = findViewById(R.id.profile)
        messageButton.setOnClickListener {
            val intent = Intent(this, MessageActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


        // invoking the search dialog
        searchView = findViewById(R.id.searchView)
        recyclerView = findViewById(R.id.marketplaceRecyclerView)

        // TODO: Set up marketplaceAdapter and marketplaceItem to uncomment this searchbar code
//        marketplaceAdapter = MarketplaceAdapter(marketplaceList)
//        recyclerView.adapter = marketplaceAdapter
//        listenForMarketplaceUpdates()
//
//        val searchHelper = SearchHelper(
//            context = this,
//            searchView = searchView,
//            recyclerView = recyclerView,
//            adapter = marketplaceAdapter,
//            dataList = marketplaceList,
//            filterFunction = ::filterMarketplaceItems  // Pass filter function to the helper
//        )


//    // filters marketplace items based on the query
//    private fun filterMarketplaceItems(query: String): List<MarketplaceItem> {
//        return marketplaceList.filter { item ->
//            item.itemName.contains(query, ignoreCase = true) ||
//                    item.description.contains(query, ignoreCase = true)
//        }
//    }
//
//    private fun listenForMarketplaceUpdates() {
//        fun listenForEventsUpdate() {
//            db.collection("marketplace")
//                .addSnapshotListener { snapshot, e ->
//                    if (e != null) {
//                        Toast.makeText(this, "Error fetching marketplace items", Toast.LENGTH_SHORT)
//                            .show()
//                        return@addSnapshotListener
//                    }
//                    val updatedMarketplaceItem = mutableListOf<MarketplaceItem>()
//                    for (document in snapshot!!.documents) {
//                        val marketplaceItem = document.toObject(MarketplaceItem::class.java)
//                        if (marketplaceItem != null) {
//                            updatedMarketplaceItems.add(marketplaceItem)
//                        }
//                    }
//                    marketplaceAdapter.updateEvents(updatedMarketplaceItems)
//                }
//        }
//    }

        val createListingButton = findViewById<ImageButton>(R.id.make_listing_button)
        createListingButton.setOnClickListener {
            NewMarketItemDialog()
        }

}

    private fun listenForItemsUpdate() {
        db.collection("items")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Error fetching events", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                itemList.clear()
                for (document in snapshot!!.documents) {
                    val item = document.toObject(Marketplace::class.java)
                    if (item != null) {
                        itemList.add(item)
                    }
                }
                marketplaceAdapter.notifyDataSetChanged()
            }
    }

    private fun NewMarketItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.new_listing_dialog, null)
        val itemNameEditText = dialogView.findViewById<EditText>(R.id.newItemName)
        val priceEditText = dialogView.findViewById<EditText>(R.id.newItemPrice)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.newItemDescription)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val buttonConfirm = dialogView.findViewById<Button>(R.id.buttonConfirm)
        val pickImageButton: Button = dialogView.findViewById(R.id.upload_a_picture)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }
        // Trigger the image picker when the button is clicked
        pickImageButton.setOnClickListener {
            getContent.launch("image/*")
        }
        buttonConfirm.setOnClickListener {
            val itemName = itemNameEditText.text.toString()
            val price = priceEditText.text.toString()
            val description = descriptionEditText.text.toString()
            if (itemName.isNotEmpty() && price.isNotEmpty() && description.isNotEmpty() && selectedPhotoUri != null) {
                uploadItemAndCreateListing(itemName, price, description)
                Toast.makeText(this, "Item Created: $itemName", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill in all fields and select an image", Toast.LENGTH_LONG).show()
                }
        }
        dialog.show()
    }

    fun uploadItemAndCreateListing(itemName: String, price: String, description: String) {
        val storageRef =
            FirebaseStorage.getInstance().reference.child("items/${UUID.randomUUID()}.jpg")

        selectedPhotoUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val newItem = Marketplace(
                            userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                            userID = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            itemName = itemName,
                            price = price,
                            description = description,
                            itemPhotoURL = downloadUri.toString(),
                            time = System.currentTimeMillis().toString(),
                            itemID = UUID.randomUUID().toString()
                        )
                        newItem.createItemListing()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
        }
    }
}