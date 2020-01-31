package app.opass.ccip.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.opass.ccip.BuildConfig
import app.opass.ccip.R

class AboutActivity : AppCompatActivity() {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val appIcon by lazy { findViewById<ImageView>(R.id.app_icon) }
    private val appName by lazy { findViewById<TextView>(R.id.app_name) }
    private val appVersion by lazy { findViewById<TextView>(R.id.app_version) }
    private val rv by lazy { findViewById<RecyclerView>(R.id.rv) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setTitle(R.string.about_app)

        appIcon.setImageResource(applicationInfo.icon)
        appName.setText(R.string.app_name)

        val version = "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"
        appVersion.text = resources.getString(R.string.version, version)

        val items = listOf(
            LinkItem(R.drawable.github_mark, "https://github.com/CCIP-App/CCIP-Android", R.string.star_on_github),
            LinkItem(R.drawable.baseline_policy_24, "https://opass.app/privacy-policy.html", R.string.privacy_policy)
        )

        rv.adapter = LinkAdapter(items) {
            startActivity(Intent(Intent.ACTION_VIEW, it.url.toUri()))
        }
        rv.layoutManager = LinearLayoutManager(this)
    }
}

private data class LinkItem(val icon: Int, val url: String, val title: Int)

private class LinkAdapter(
    private val items: List<LinkItem>,
    private val onItemClick: (LinkItem) -> Unit
) : RecyclerView.Adapter<LinkAdapter.LinkViewHolder>() {
    class LinkViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView= view.findViewById(R.id.icon)
        val titleView: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinkViewHolder =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu_item, parent, false)
            .let(::LinkViewHolder)
            .apply {
                itemView.setOnClickListener {
                    onItemClick(items[adapterPosition])
                }
            }

    override fun onBindViewHolder(holder: LinkViewHolder, position: Int) {
        val item = items[position]
        holder.iconView.setImageResource(item.icon)
        holder.titleView.setText(item.title)
    }

    override fun getItemCount() = items.size
}
