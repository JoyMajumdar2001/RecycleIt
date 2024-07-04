package com.qdot.recycleit

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.app.imagepickerlibrary.ImagePicker
import com.app.imagepickerlibrary.ImagePicker.Companion.registerImagePicker
import com.app.imagepickerlibrary.listener.ImagePickerResultListener
import com.app.imagepickerlibrary.model.PickerType
import com.google.ai.client.generativeai.GenerativeModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.qdot.recycleit.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

class MainActivity : AppCompatActivity(), ImagePickerResultListener {
    private lateinit var binding: ActivityMainBinding
    private val imagePicker: ImagePicker by lazy {
        registerImagePicker(this)
    }
    private val labeler : ImageLabeler by lazy {
        ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }
    private val generativeModel : GenerativeModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = getString(R.string.ai_key)
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.indicatorLine.visibility = View.GONE

        imagePicker.title("Pick recyclable item")
            .multipleSelection(false)
            .allowCropping(false)

        binding.extendedFab.setOnClickListener {
            imagePicker.open(PickerType.CAMERA)
        }
    }

    override fun onImagePick(uri: Uri?) {
        binding.noImgText.visibility = View.GONE
        binding.indicatorLine.visibility = View.VISIBLE
        val image = InputImage.fromFilePath(this,uri!!)
        binding.itemImage.load(uri)
        labeler.process(image).addOnSuccessListener { labels ->
            val imageCaption = labels[0].text
            val prompt = "You are a recycle bot. You give quick steps to recycle, reduce or reuse waste material.\n" +
                    "Keep the ideas short and simple. Try to answer in less than 150 words. Use a bullet point format to make it more readable.\n" +
                    "Tell me what I can do with $imageCaption"
            CoroutineScope(Dispatchers.IO).launch {
                val response = generativeModel.generateContent(prompt)
                val flavour = CommonMarkFlavourDescriptor()
                val parsedTree = MarkdownParser(flavour).buildMarkdownTreeFromString(response.text!!)
                val html = HtmlGenerator(response.text!!, parsedTree, flavour).generateHtml()
                withContext(Dispatchers.Main){
                    binding.indicatorLine.visibility = View.GONE
                    binding.typeTextView.animateText(Html.fromHtml(html,Html.FROM_HTML_MODE_COMPACT))
                }
            }
        }
    }

    override fun onMultiImagePick(uris: List<Uri>?) {

    }
}