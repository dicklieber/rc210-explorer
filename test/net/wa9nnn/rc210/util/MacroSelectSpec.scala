/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.wa9nnn.rc210.util

import com.wa9nnn.util.tableui.Cell
import net.wa9nnn.rc210.data.FieldKey
import net.wa9nnn.rc210.data.field.RenderMetadata
import net.wa9nnn.rc210.key.KeyFactory
import org.specs2.mutable.Specification

class MacroSelectSpec extends Specification {
  private val renderMetadata = new RenderMetadata {
    override def param = FieldKey("to Run", KeyFactory.defaultMacroKey).param

    override def prompt = "macro"

    override def unit = ""
  }
  "MacroSelect" should {
    val macroSelect = MacroSelect()
    "initial state" >> {
      macroSelect.value must beEqualTo(KeyFactory.defaultMacroKey)

    }

    "toHtmlField" in {
      val html = macroSelect.toHtmlField(renderMetadata)
      html must beEqualTo("""
                            |    <select name="Macro|macroKey1" class="form-select" aria-label="Default select example">
                            |    <option value="macroKey1" >macroKey1</option>
                            |<option value="macroKey2" >macroKey2</option>
                            |<option value="macroKey3" >macroKey3</option>
                            |<option value="macroKey4" >macroKey4</option>
                            |<option value="macroKey5" >macroKey5</option>
                            |<option value="macroKey6" >macroKey6</option>
                            |<option value="macroKey7" >macroKey7</option>
                            |<option value="macroKey8" >macroKey8</option>
                            |<option value="macroKey9" >macroKey9</option>
                            |<option value="macroKey10" >macroKey10</option>
                            |<option value="macroKey11" >macroKey11</option>
                            |<option value="macroKey12" >macroKey12</option>
                            |<option value="macroKey13" >macroKey13</option>
                            |<option value="macroKey14" >macroKey14</option>
                            |<option value="macroKey15" >macroKey15</option>
                            |<option value="macroKey16" >macroKey16</option>
                            |<option value="macroKey17" >macroKey17</option>
                            |<option value="macroKey18" >macroKey18</option>
                            |<option value="macroKey19" >macroKey19</option>
                            |<option value="macroKey20" >macroKey20</option>
                            |<option value="macroKey21" >macroKey21</option>
                            |<option value="macroKey22" >macroKey22</option>
                            |<option value="macroKey23" >macroKey23</option>
                            |<option value="macroKey24" >macroKey24</option>
                            |<option value="macroKey25" >macroKey25</option>
                            |<option value="macroKey26" >macroKey26</option>
                            |<option value="macroKey27" >macroKey27</option>
                            |<option value="macroKey28" >macroKey28</option>
                            |<option value="macroKey29" >macroKey29</option>
                            |<option value="macroKey30" >macroKey30</option>
                            |<option value="macroKey31" >macroKey31</option>
                            |<option value="macroKey32" >macroKey32</option>
                            |<option value="macroKey33" >macroKey33</option>
                            |<option value="macroKey34" >macroKey34</option>
                            |<option value="macroKey35" >macroKey35</option>
                            |<option value="macroKey36" >macroKey36</option>
                            |<option value="macroKey37" >macroKey37</option>
                            |<option value="macroKey38" >macroKey38</option>
                            |<option value="macroKey39" >macroKey39</option>
                            |<option value="macroKey40" >macroKey40</option>
                            |<option value="macroKey41" >macroKey41</option>
                            |<option value="macroKey42" >macroKey42</option>
                            |<option value="macroKey43" >macroKey43</option>
                            |<option value="macroKey44" >macroKey44</option>
                            |<option value="macroKey45" >macroKey45</option>
                            |<option value="macroKey46" >macroKey46</option>
                            |<option value="macroKey47" >macroKey47</option>
                            |<option value="macroKey48" >macroKey48</option>
                            |<option value="macroKey49" >macroKey49</option>
                            |<option value="macroKey50" >macroKey50</option>
                            |<option value="macroKey51" >macroKey51</option>
                            |<option value="macroKey52" >macroKey52</option>
                            |<option value="macroKey53" >macroKey53</option>
                            |<option value="macroKey54" >macroKey54</option>
                            |<option value="macroKey55" >macroKey55</option>
                            |<option value="macroKey56" >macroKey56</option>
                            |<option value="macroKey57" >macroKey57</option>
                            |<option value="macroKey58" >macroKey58</option>
                            |<option value="macroKey59" >macroKey59</option>
                            |<option value="macroKey60" >macroKey60</option>
                            |<option value="macroKey61" >macroKey61</option>
                            |<option value="macroKey62" >macroKey62</option>
                            |<option value="macroKey63" >macroKey63</option>
                            |<option value="macroKey64" >macroKey64</option>
                            |<option value="macroKey65" >macroKey65</option>
                            |<option value="macroKey66" >macroKey66</option>
                            |<option value="macroKey67" >macroKey67</option>
                            |<option value="macroKey68" >macroKey68</option>
                            |<option value="macroKey69" >macroKey69</option>
                            |<option value="macroKey70" >macroKey70</option>
                            |<option value="macroKey71" >macroKey71</option>
                            |<option value="macroKey72" >macroKey72</option>
                            |<option value="macroKey73" >macroKey73</option>
                            |<option value="macroKey74" >macroKey74</option>
                            |<option value="macroKey75" >macroKey75</option>
                            |<option value="macroKey76" >macroKey76</option>
                            |<option value="macroKey77" >macroKey77</option>
                            |<option value="macroKey78" >macroKey78</option>
                            |<option value="macroKey79" >macroKey79</option>
                            |<option value="macroKey80" >macroKey80</option>
                            |<option value="macroKey81" >macroKey81</option>
                            |<option value="macroKey82" >macroKey82</option>
                            |<option value="macroKey83" >macroKey83</option>
                            |<option value="macroKey84" >macroKey84</option>
                            |<option value="macroKey85" >macroKey85</option>
                            |<option value="macroKey86" >macroKey86</option>
                            |<option value="macroKey87" >macroKey87</option>
                            |<option value="macroKey88" >macroKey88</option>
                            |<option value="macroKey89" >macroKey89</option>
                            |<option value="macroKey90" >macroKey90</option>
                            |<option value="macroKey91" >macroKey91</option>
                            |<option value="macroKey92" >macroKey92</option>
                            |<option value="macroKey93" >macroKey93</option>
                            |<option value="macroKey94" >macroKey94</option>
                            |<option value="macroKey95" >macroKey95</option>
                            |<option value="macroKey96" >macroKey96</option>
                            |<option value="macroKey97" >macroKey97</option>
                            |<option value="macroKey98" >macroKey98</option>
                            |<option value="macroKey99" >macroKey99</option>
                            |<option value="macroKey100" >macroKey100</option>
                            |<option value="macroKey101" >macroKey101</option>
                            |<option value="macroKey102" >macroKey102</option>
                            |<option value="macroKey103" >macroKey103</option>
                            |<option value="macroKey104" >macroKey104</option>
                            |<option value="macroKey105" >macroKey105</option>
                            |    </select>
                            |    """.stripMargin)
    }

  }
}
