package jetboot

//TODO: should I be Styleable? maybe
//TODO: should I be Hideable ? almost definitely
//TODO: or should probably keep this trait to the minimum and mixin Hideable, Disableable etc - nah have a lower level trait for that
trait Widget extends Identifiable with Renderable

