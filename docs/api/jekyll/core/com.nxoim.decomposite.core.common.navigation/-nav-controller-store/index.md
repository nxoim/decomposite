---
title: NavControllerStore
---
//[core](../../../index.html)/[com.nxoim.decomposite.core.common.navigation](../index.html)/[NavControllerStore](index.html)



# NavControllerStore



[common]\
@[Immutable](https://developer.android.com/reference/kotlin/androidx/compose/runtime/Immutable.html)



class [NavControllerStore](index.html)

Simple store for navigation controllers.



## Constructors


| | |
|---|---|
| [NavControllerStore](-nav-controller-store.html) | [common]<br>constructor() |


## Functions


| Name | Summary |
|---|---|
| [get](get.html) | [common]<br>fun &lt;[T](get.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [get](get.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, kClass: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;[T](get.html)&gt;): [NavController](../-nav-controller/index.html)&lt;[T](get.html)&gt;?<br>DO NOT CALL DIRECTLY. Use [navController](../nav-controller.html) |
| [getOrCreate](get-or-create.html) | [common]<br>fun &lt;[T](get-or-create.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [getOrCreate](get-or-create.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, kClass: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;[T](get-or-create.html)&gt;, creator: () -&gt; [NavController](../-nav-controller/index.html)&lt;[T](get-or-create.html)&gt;): [NavController](../-nav-controller/index.html)&lt;[T](get-or-create.html)&gt;<br>DO NOT CALL DIRECTLY. Use [navController](../nav-controller.html) |
| [remove](remove.html) | [common]<br>fun &lt;[T](remove.html) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)&gt; [remove](remove.html)(key: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null, kClass: [KClass](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.reflect/-k-class/index.html)&lt;[T](remove.html)&gt;)<br>DO NOT CALL DIRECTLY. Use [navController](../nav-controller.html) |

