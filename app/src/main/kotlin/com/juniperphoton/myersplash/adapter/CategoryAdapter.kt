package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R

class CategoryAdapter(private val context: Context,
                      private val list: Array<String>
) : androidx.recyclerview.widget.RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    companion object ResMap {
        val KEYWORDS: Array<String> = App.instance.resources.getStringArray(R.array.search_category)!!
    }

    /**
     * Invoked when item clicked.
     */
    var onClickItem: ((string: String) -> Unit)? = null

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[holder.adapterPosition])
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.row_search_category, parent, false))
    }

    inner class CategoryViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.category_text)
        lateinit var categoryName: TextView

        private var category: String? = null

        init {
            ButterKnife.bind(this, itemView)
            itemView.setOnClickListener {
                category?.let {
                    onClickItem?.invoke(it)
                }
            }
        }

        fun bind(cate: String) {
            category = cate
            categoryName.text = cate
        }
    }
}
