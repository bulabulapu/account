package com.tang.account.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tang.account.R
import com.tang.account.logic.util.RecordTransformer
import com.tang.account.viewmodel.DetailViewModel
import de.hdodenhof.circleimageview.CircleImageView

/*DetailActivity的类别选择弹窗中的RecyclerAdapter*/
class CategoryRecyclerAdapter(
    private val viewModel: DetailViewModel,
    private val dialogFragment: CategoryDialogFragment
) :
    RecyclerView.Adapter<CategoryRecyclerAdapter.ViewHolder>() {

    /*类别列表*/
    private val list = RecordTransformer.categoryList

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.category_text)
        val image: CircleImageView = view.findViewById(R.id.category_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.dialog_category_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = RecordTransformer.transform(list[position])
        holder.image.setImageResource(RecordTransformer.getImageId(list[position]))
        holder.image.setOnClickListener {
            viewModel.setCategory(list[position])
            dialogFragment.dismiss()
        }
        holder.text.setOnClickListener {
            viewModel.setCategory(list[position])
            dialogFragment.dismiss()
        }
    }
}