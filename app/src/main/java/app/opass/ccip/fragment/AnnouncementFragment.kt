package app.opass.ccip.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.opass.ccip.R
import app.opass.ccip.adapter.AnnouncementAdapter
import app.opass.ccip.model.Announcement
import app.opass.ccip.network.CCIPClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnnouncementFragment : Fragment() {
    private lateinit var announcementView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mActivity: Activity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_announcement, container, false)

        announcementView = view.findViewById(R.id.announcement)
        swipeRefreshLayout = view.findViewById(R.id.swipeContainer)

        mActivity = requireActivity()
        announcementView.layoutManager = LinearLayoutManager(mActivity)
        announcementView.itemAnimator = DefaultItemAnimator()

        swipeRefreshLayout.isEnabled = false
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }

        CCIPClient.get().announcement().enqueue(object : Callback<List<Announcement>> {
            override fun onResponse(call: Call<List<Announcement>>, response: Response<List<Announcement>>) {
                swipeRefreshLayout.isRefreshing = false
                if (response.isSuccessful && response.body()?.isEmpty() == false) {
                    announcementView.adapter = AnnouncementAdapter(mActivity, response.body()!!)
                } else {
                    view.findViewById<View>(R.id.announcement_empty).visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<List<Announcement>>, t: Throwable) {
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(mActivity, R.string.offline, Toast.LENGTH_LONG).show()
            }
        })

        return view
    }
}
