package com.example.derek.trademeapi.ui.listings

import android.content.Context
import android.content.res.Configuration
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.derek.trademeapi.BR
import com.example.derek.trademeapi.R
import com.example.derek.trademeapi.base.BaseActivity
import com.example.derek.trademeapi.model.Category
import com.example.derek.trademeapi.model.Listing
import com.example.derek.trademeapi.ui.components.GridEndlessRecyclerViewScrollListener
import com.example.derek.trademeapi.util.GridLayoutColumnQty
import com.example.derek.trademeapi.util.bindView
import timber.log.Timber
import javax.inject.Inject


/**
 * Created by derek on 30/04/18.
 */
class ListingActivity : BaseActivity(), ListingView {

    @Inject
    override lateinit var presenter: ListingPresenter

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val rootCoordinatorLayout: CoordinatorLayout by bindView(R.id.root)
    private val adapter: Adapter by lazy { Adapter(presenter) }
    private lateinit var gridLayoutManager : GridLayoutManager
    private lateinit var listingScrollListener : GridEndlessRecyclerViewScrollListener

    private val listingRecyclerView: RecyclerView by bindView(R.id.recycler_view)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listing)
        setSupportActionBar(toolbar)

        val portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        val gridLayoutColumnQty = GridLayoutColumnQty(applicationContext, R.layout.view_listing_item)
        val column = if (portrait) 2 else gridLayoutColumnQty.calculateNoOfColumns()

        listingRecyclerView.layoutManager = GridLayoutManager(getContext(), column).also { gridLayoutManager = it}
        listingRecyclerView.adapter = adapter

        listingScrollListener = GridEndlessRecyclerViewScrollListener(gridLayoutManager,
                object : GridEndlessRecyclerViewScrollListener.DataLoader{
                    override fun onLoadMore(): Boolean = presenter.loadMoreListings(1)
                })
        listingRecyclerView.addOnScrollListener(listingScrollListener)
        Timber.d("presenter: $presenter")
    }

    override fun onResume() {
        super.onResume()
        presenter.onViewCreated()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onViewDestroyed()
    }

    override fun getContext(): Context? = this.applicationContext


    override fun setCurrentCategory(currentCategory: Category) {
        Timber.d("setCurrentCategory: $currentCategory")
    }


    /** listing */



    override fun updateListings(listings: MutableList<Listing>, from: Int?, to: Int?, operation: ListingView.Notify?) {
        Timber.d("updateListings length: ${listings.size} from: $from, to: $to")

        when (operation) {
            ListingView.Notify.INSERT -> {
                adapter.notifyItemInserted(from ?: 0)
            }
            ListingView.Notify.CLEAR -> {
                adapter.notifyItemRangeRemoved(0, to ?: listings.size)
            }
            ListingView.Notify.UPDATE -> {
                adapter.notifyItemRangeChanged(from ?: 0, to ?: listings.size)
            }
            ListingView.Notify.REMOVE -> {
                adapter.notifyItemRangeRemoved(from ?: 0, to ?: listings.size)
            }
            null -> {
                adapter.notifyDataSetChanged()
            }
        }
    }

    override fun scrollToPosition(position: Int) {
        listingRecyclerView.scrollToPosition(position)
    }

    override fun showProgress() {
        Timber.d("loading started ....................... ")
    }

    override fun hideProgress() {
        Timber.d(" ....................... loading stopped")
        Snackbar.make(rootCoordinatorLayout, "content loading finished, total: ${presenter.getListingSize()}", Snackbar.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Timber.e("loading error: $message")
        Snackbar.make(rootCoordinatorLayout, message, Snackbar.LENGTH_LONG).show()

    }

    /** recycler view */


    class ItemViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Listing) {
            binding.setVariable(BR.listing, item)
            binding.executePendingBindings()
        }
    }

    class Adapter(private val presenter: ListingPresenter) : RecyclerView.Adapter<ItemViewHolder>() {
//        @Inject lateinit var presenter: ListingPresenter //TODO: why doesn't this work?

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, R.layout.view_listing_item, parent, false)
            return ItemViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return presenter.getListingSize()
        }
//        override fun getItemCount(): Int = presenter.getListingSize()

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val listing = presenter.getListingAtIndex(position)
            holder.bind(listing)
        }
    }


}