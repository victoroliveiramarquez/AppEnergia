package com.aplicacion2.appenergia.samartsolar

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aplicacion2.appenergia.samartsolar.fragments.DetallesFragment
import com.aplicacion2.appenergia.samartsolar.fragments.EnergiaFragment
import com.aplicacion2.appenergia.samartsolar.fragments.InstalacionFragment

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> InstalacionFragment()  // Fragmento de Mi instalación
            1 -> EnergiaFragment()        // Fragmento de Energía
            2 -> DetallesFragment()       // Fragmento de Detalles
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}
