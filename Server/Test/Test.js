
console.log(PadLeft(1, 3));
console.log(PadLeft(2, 3));
console.log(PadLeft(10, 3));
console.log(PadLeft(99, 3));
console.log(PadLeft(100, 3));
function PadLeft (num, n) {
	var txt = Math.pow(10,n) + num + '';
	console.log(txt);
    return (Math.pow(10,n) + num + '').substr(1);
}