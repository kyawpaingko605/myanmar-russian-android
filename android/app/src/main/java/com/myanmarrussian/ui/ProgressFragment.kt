package com.myanmarrussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.myanmarrussian.databinding.FragmentProgressBinding

/**
 * ProgressFragment - Equivalent to iOS ProgressView
 * Shows learning progress stats and category breakdown
 */
class ProgressFragment : Fragment() {

    private var _binding: FragmentProgressBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgressBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Progress data is static/hardcoded matching iOS version
        // In a real app, this would be loaded from a database
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
