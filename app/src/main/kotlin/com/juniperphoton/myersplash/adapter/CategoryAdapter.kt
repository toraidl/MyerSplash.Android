package com.juniperphoton.myersplash.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juniperphoton.myersplash.App
import com.juniperphoton.myersplash.R

typealias OnClickCategoryItemListener = ((string: String) -> Unit)

class CategoryAdapter(private val context: Context,
                      private val list: Array<String>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    companion object ResMap {
        val KEYWORDS: Array<String> = App.instance.resources.getStringArray(R.array.search_category)
    }

    /**
     * Invoked when item clicked.
     */
    var onClickItem: OnClickCategoryItemListener? = null

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[holder.adapterPosition])
    }

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(LayoutInflater.from(context).inflate(R.layout.row_search_category, parent, false))
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var categoryName: TextView = itemView.findViewById(R.id.category_text)

        private var category: String? = null

        init {
            itemView.setOnClickListener {
                category?.let { c ->
                    onClickItem?.invoke(c)
                }
            }
        }

        fun bind(cate: String) {
            category = cate
            categoryName.text = cate
        }
    }
}
