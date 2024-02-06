package com.number869.decomposite.core.common.ultils

import com.arkivanov.essenty.statekeeper.SerializableContainer

fun tryRestoreStateFromFile(): SerializableContainer? {
    return null
//    return File("states.dat").takeIf(File::exists)?.let { file ->
//        try {
//            ObjectInputStream(file.inputStream())
//                .use(ObjectInputStream::readObject) as SerializableContainer
//        } catch (e: Exception) {
//            null
//        } finally {
//            file.delete()
//        }
//    }
}