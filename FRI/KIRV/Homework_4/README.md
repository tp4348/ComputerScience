# Homework 4

## Part one

- Let n = pq be a RSA modulus, and d be the encryption exponent. Show that there are at least 9 distinct cleartexts mm that are invariant under encryption, i.e. m^d \equiv m \pmod{n}m
d≡ m(mod n).

		Ker sta p in q različni lihi praštevili, sledi iz:

			m^d = m (mod p)
			m^d = m (mod q)
	
		da kot posledica Chinese Remainder Theorema(CRT) velja tudi:

			m^d = m (mod n)

		DOKAZ: Rešitev sistema

			m^d = m (mod p)
			m^d = m (mod q)
		
		je po postopku CRT enaka

			m^d = mq*q^-1 + mp*p^-1 (mod pq)
			m^d = m (q*q^-1 + p*p^-1) (mod n)
		
		Pri čemer je izraz v oklepaju enak 1, saj velja

			gcd(p, q) = 1 oz. p*a + q*b = 1

		in zato tudi velja:

			m^d = m (mod n)

		Za vsako izmed teh enačb imamo tri rešitve {0, 1, -1}:
		- 0 in 1 sta trivialni rešitvi enačbe, saj:

			0^d = 0 (mod n,p,q)
			1^d = 1 (mod n,p,q) 

		- -1 pa je rešitev, ker je d lih, saj:

			(-1)^d = -1 (mod n,p,q)

		katerikoli lih koeficient bo d bo enačba veljala.
		
		Torej našli smo 9 tekstov neodvisnih od enkripcije, 3 za vsako enačbo.

- Prove that in RSA cryptosystem decryption is inverse operation of encryption for all plaintexts x \in \Zx∈ Z (including x \in \Z_n \setminus \Z^∗_nx∈ Zn∖Zn∗).

		Za najmanjši skupni večkratnik velja sledeče(L je lambda funkcija):

			L(pq) = lcm(p-1,q-1)
		
		Iz tega lahko zapišemo:
			
			ed - 1 = h(p-1) = k(q-1)

		za poljubni nenegativni števili k in h.
		Iz prve naloge vemo da, če veljata izraza:

			x^(ed) = x (mod p) (enačba 1)
			x^(ed) = x (mod q) (enačba 2)

		velja tudi:

			x^(ed) = x (mod pq)
			
		Da sočasno preverimo veljavnosti enačb 1 in 2, sprva preverimo enakosti:

			x = 0 (mod p)
			x = 0 (mod q)

		kar pomeni, da je x večkratnik p oz. q torej je tudi x^(ed) večkratnik p oz. q.
		Torej velja:

			x^(ed) = 0 = x (mod p)

		Nato preverimo še enakosti:

			x != 0 (mod p)
			x != 0 (mod q)

		Slednji enačbi razvijemo:

			x^(ed) = x^(ed-1)*x = x^(h(p-1))*x = ((x^(p-1))^h)*x
			x^(ed) = x^(ed-1)*x = x^(k(q-1))*x = ((x^(q-1))^k)*x

		In ker po Fermatovem teoremu velja:

			x^(p-1) = 1 (mod p)
			x^(q-1) = 1 (mod q)

		za katerokoli število x in praštevilo p oz. q, dobimo:

			((x^(p-1))^h)*x = (1^h)*x = x (mod p)
			((x^(q-1))^k)*x = (1^k)*x = x (mod q)

		S tem smo dokazali, da velja:

			x^(ed) = x (mod pq)

		za vsak x€Z in za e in d za katera velja:

			ed = 1 (mod L(pq))
			
			

## Part two

Let us demonstrate that for a secure RSA modulus n = pq, the difference |p - q| should not be too small.
- Let d be an integer such that q - p = 2d > 0. Prove that n + d^2 is a perfect square.

		Naj bo d najmanjše možno naravno število, da bo veljalo d^2 > n.
		Če velja da je d^2 - n = h^2 (popoln kvadrat) potem, lahko razstavimo n na n = (d + h)*(d - h) = p*q in dobimo faktorja p in q. 
		Če ne dobimo popolnega kvadrata, rekurzivno poskušamo na sledeč način:
		(d+1)^2 - n -> ali je to popolni kvadrat?
		(d+2)^2 - n -> ali je to popolni kvadrat?
		...
		Eventuelno dobimo kvadrat, saj velja ((x + 1)/2)^2 - x = ((x-1)/2)^2

		Ta algoritem imenujemo Fermatov Faktorizacijski algoritem (Fermat’s factorization algorithm)

- You have just found that n + d^2 is a perfect square for some small integer d. Can you factorize nn efficiently in this case?

		Ja, za majhno razliko med faktorji oz. majhen d je Fermatov Faktorizacijski algoritem precej efektiven.

## Part three

- Koda:

![image](https://user-images.githubusercontent.com/48418580/142741026-b2115cb7-1025-447f-b5b1-be67d3a39135.png)

- Vrednosti p in q:

		p = 1367325379127018066307214452947735693410899149678183367457725246763138525564561713653812587568218809390154835824885642545768933745664691629459271126259992540697580325946658912148177178365217491813779546617070121771967084092625291715407026859798403970571092289807643066444062232012316457691541712659582676759382798758754053785169197815169846935072180629146239845560587584917629562604108515222609909400843721158027075355107257623104603715446366081082475900133916721
		q = 1367325379127018066307214452947735693410899149678183367457725246763138525564561713653812587568218809390154835824885642545768933745664691629459271126259992540697580325946658912148177178365217491813779546617070121771967084092625291715407026859798403970571092289807643066444062232012316457691541712659582676759382798758754053785169197815169846935072180629146239845560587584917629562604108515222609909400843721158027075355107257623104603715446366081082475900133906791
		
- Razlika med p in q je majhna:

		p - q = 9930

- Dekriptirano sporočilo:
		
		"Suppose that p_1 = 2 < p_2 = 3 < ... < p_r are all of the primes. Let P = p_1 p_2 ... p_r + 1 and let p be a prime dividing P; then p can not be any of p_1, p_2, ..., p_r, otherwise p would divide the difference P - p_1 p_2 ... p_r = 1, which is impossible. So this prime p is still another prime, and p_1, p_2, ..., p_r would not be all of the primes."
 		-- Euclid.
		With love, Bob




