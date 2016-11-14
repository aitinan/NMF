package com.chen.pca;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;

import java.io.Serializable;

class Svd implements Serializable {

	private int m;
	private int n;
	private DoubleMatrix2D A;
	private DoubleMatrix2D U;
	//private DoubleMatrix2D singular;
	private double[] s;
	
	Svd(DoubleMatrix2D Zui, int userNumber, int itemNumber) {
		this.A = Zui;
		this.m = userNumber;
		this.n = itemNumber;
	}

	void svd() {
		
		int nu = Math.min(m, n);
		U = new DenseDoubleMatrix2D(m, nu);
		DoubleMatrix2D V = new DenseDoubleMatrix2D(n, n);
		s = new double[Math.min(m + 1, n)];
		double[] e = new double[n];
		double[] work = new double[m];
		
		int nct = Math.min(m - 1, n);
		int nrt = Math.max(0, Math.min(n - 2, m));
		
		for (int k = 0; k < Math.max(nct, nrt); k++) {
			if (k < nct) {
				s[k] = 0;
				for (int i = k; i < m; i++) {
					s[k] = Util.hypot(s[k], A.getQuick(i, k));
				}
				if (s[k] != 0.0) {
					if (A.getQuick(k, k) < 0.0) {
						s[k] = -s[k];
					}
					for (int i = k; i < m; i++) {
						A.setQuick(i, k, A.getQuick(i, k) / s[k]);
					}
					A.setQuick(k, k, A.getQuick(k, k) + 1.0);
				}
				s[k] = -s[k];
			}
			
			for (int j = k + 1; j < n; j++) {
				if ((k < nct) & (s[k] != 0.0)) {
					double t = 0;
					for (int i = k; i < m; i++) {
						t += A.getQuick(i, k) * A.getQuick(i, j);
					}
					t = -t / A.getQuick(k, k);
					for (int i = k; i < m; i++) {
						A.setQuick(i, j, A.getQuick(i, j) + t * A.getQuick(i, k));
					}
				}
				e[j] = A.getQuick(k, j);
			}
			
			if (k < nct) {
				for (int i = k; i < m; i++) {
					U.setQuick(i, k, A.getQuick(i, k));
				}
			}
			
			if (k < nrt) {
				e[k] = 0;
				for (int i = k + 1; i < n; i++) {
					e[k] = Util.hypot(e[k], e[i]);
				}
				if (e[k] != 0.0) {
					if (e[k + 1] < 0.0) {
						e[k] = -e[k];
					}
					for (int i = k + 1; i < n; i++) {
						e[i] /= e[k];
					}
					e[k + 1] += 1.0;
				}
				e[k] = -e[k];
				
				if ((k + 1 < m) & (e[k] != 0.0)) {
					for (int i = k + 1; i < m; i++) {
						work[i] = 0.0;
					}
					for (int j = k + 1; j < n; j++) {
						for (int i = k + 1; i < m; i++) {
							work[i] += e[j] * A.getQuick(i, j);
						}
					}
					for (int j = k + 1; j < n; j++) {
						double t = -e[j] / e[k + 1];
						for (int i = k + 1; i < m; i++) {
							A.setQuick(i, j, A.getQuick(i, j) + t * work[i]);
						}
					}
				}
				

				for (int i = k + 1; i < n; i++) {
					V.setQuick(i, k, e[i]);
				}
			}
		}
		
		int p = Math.min(n, m + 1);
		if (nct < n) {
			s[nct] = A.getQuick(nct, nct);
		}
		if (m < p) {
			s[p - 1] = 0.0;
		}
		if (nrt + 1 < p) {
			e[nrt] = A.getQuick(nrt, p - 1);
		}
		e[p - 1] = 0.0;


		for (int j = nct; j < nu; j++) {
			for (int i = 0; i < m; i++) {
				U.setQuick(i, j, 0.0);
			}
			U.setQuick(j, j, 1.0);
		}

		for (int k = nct - 1; k >= 0; k--) {
			if (s[k] != 0.0) {
				for (int j = k + 1; j < nu; j++) {
					double t = 0;
					for (int i = k; i < m; i++) {
						t += U.getQuick(i, k) * U.getQuick(i, j);
					}
					t = -t / U.getQuick(k, k);
					for (int i = k; i < m; i++) {
						U.setQuick(i, j, U.getQuick(i, j) + t * U.getQuick(i, k));
					}
				}
				for (int i = k; i < m; i++) {
					U.setQuick(i, k, -U.getQuick(i, k));
				}
				U.setQuick(k, k, U.getQuick(k, k) + 1.0);
				for (int i = 0; i < k - 1; i++) {
					U.setQuick(i, k, 0.0);
				}
			} else {
				for (int i = 0; i < m; i++) {
					U.setQuick(i, k, 0.0);
				}
				U.setQuick(k, k, 1.0);
			}
		}

		for (int k = n - 1; k >= 0; k--) {
			if ((k < nrt) & (e[k] != 0.0)) {
				for (int j = k + 1; j < nu; j++) {
					double t = 0;
					for (int i = k + 1; i < n; i++) {
						t += V.getQuick(i, k) * V.getQuick(i, j);
					}
					t = -t / V.getQuick(k + 1, k);
					for (int i = k + 1; i < n; i++) {
						V.setQuick(i, j, V.getQuick(i, j) + t * V.getQuick(i, k));
					}
				}
			}
			for (int i = 0; i < n; i++) {
				V.setQuick(i, k, 0.0);
			}
			V.setQuick(k, k, 1.0);
		}

		int pp = p - 1;
		int iter = 0;
		double eps = Math.pow(2.0, -52.0);
		double tiny = Math.pow(2.0, -966.0);
		
		while (p > 0) {
			int k, kase;
			for (k = p - 2; k >= -1; k--) {
				if (k == -1) {
					break;
				}
				if (Math.abs(e[k]) <= tiny + eps
						* (Math.abs(s[k]) + Math.abs(s[k + 1]))) {
					e[k] = 0.0;
					break;
				}
			}
			if (k == p - 2) {
				kase = 4;
			} else {
				int ks;
				for (ks = p - 1; ks >= k; ks--) {
					if (ks == k) {
						break;
					}
					double t = (ks != p ? Math.abs(e[ks]) : 0.)
							+ (ks != k + 1 ? Math.abs(e[ks - 1]) : 0.);
					if (Math.abs(s[ks]) <= tiny + eps * t) {
						s[ks] = 0.0;
						break;
					}
				}
				if (ks == k) {
					kase = 3;
				} else if (ks == p - 1) {
					kase = 1;
				} else {
					kase = 2;
					k = ks;
				}
			}
			k++;

			switch (kase) {

			case 1: {
				double f = e[p - 2];
				e[p - 2] = 0.0;
				for (int j = p - 2; j >= k; j--) {
					double t = Util.hypot(s[j], f);
					double cs = s[j] / t;
					double sn = f / t;
					s[j] = t;
					if (j != k) {
						f = -sn * e[j - 1];
						e[j - 1] = cs * e[j - 1];
					}
					for (int i = 0; i < n; i++) {
						t = cs * V.getQuick(i, j) + sn * V.getQuick(i, p - 1);
						V.setQuick(i, p - 1, -sn * V.getQuick(i, j) + cs * V.getQuick(i, p - 1));
						V.setQuick(i, j, t);
					}
				}
			}
				break;

			case 2: {
				double f = e[k - 1];
				e[k - 1] = 0.0;
				for (int j = k; j < p; j++) {
					double t = Util.hypot(s[j], f);
					double cs = s[j] / t;
					double sn = f / t;
					s[j] = t;
					f = -sn * e[j];
					e[j] = cs * e[j];
					for (int i = 0; i < m; i++) {
						t = cs * U.getQuick(i, j) + sn * U.getQuick(i, k - 1);
						U.setQuick(i, k - 1, -sn * U.getQuick(i, j) + cs * U.getQuick(i, k - 1));
						U.setQuick(i, j, t);
					}
				}
			}
				break;

			case 3: {
				double scale = Math.max(
						Math.max(
								Math.max(
										Math.max(Math.abs(s[p - 1]),
												Math.abs(s[p - 2])),
										Math.abs(e[p - 2])), Math.abs(s[k])),
						Math.abs(e[k]));
				double sp = s[p - 1] / scale;
				double spm1 = s[p - 2] / scale;
				double epm1 = e[p - 2] / scale;
				double sk = s[k] / scale;
				double ek = e[k] / scale;
				double b = ((spm1 + sp) * (spm1 - sp) + epm1 * epm1) / 2.0;
				double c = (sp * epm1) * (sp * epm1);
				double shift = 0.0;
				if ((b != 0.0) | (c != 0.0)) {
					shift = Math.sqrt(b * b + c);
					if (b < 0.0) {
						shift = -shift;
					}
					shift = c / (b + shift);
				}
				double f = (sk + sp) * (sk - sp) + shift;
				double g = sk * ek;

				// Chase zeros.

				for (int j = k; j < p - 1; j++) {
					double t = Util.hypot(f, g);
					double cs = f / t;
					double sn = g / t;
					if (j != k) {
						e[j - 1] = t;
					}
					f = cs * s[j] + sn * e[j];
					e[j] = cs * e[j] - sn * s[j];
					g = sn * s[j + 1];
					s[j + 1] = cs * s[j + 1];
					for (int i = 0; i < n; i++) {
						t = cs * V.getQuick(i, j) + sn * V.getQuick(i, j + 1);
						V.setQuick(i, j + 1, -sn * V.getQuick(i, j) + cs * V.getQuick(i, j + 1));
						V.setQuick(i, j, t);
					}

					t = Util.hypot(f, g);
					cs = f / t;
					sn = g / t;
					s[j] = t;
					f = cs * e[j] + sn * s[j + 1];
					s[j + 1] = -sn * e[j] + cs * s[j + 1];
					g = sn * e[j + 1];
					e[j + 1] = cs * e[j + 1];
					if (j < m - 1) {
						for (int i = 0; i < m; i++) {
							t = cs * U.getQuick(i, j) + sn * U.getQuick(i, j + 1);
							U.setQuick(i, j + 1, -sn * U.getQuick(i, j) + cs * U.getQuick(i, j + 1));
							U.setQuick(i, j, t);
						}
					}
				}
				e[p - 2] = f;
				iter = iter + 1;
			}
				break;

			case 4: {
				if (s[k] <= 0.0) {
					s[k] = (s[k] < 0.0 ? -s[k] : 0.0);
					for (int i = 0; i <= pp; i++) {
						V.setQuick(i, k, -V.getQuick(i, k));
					}
				}

				while (k < pp) {
					if (s[k] >= s[k + 1]) {
						break;
					}
					double t = s[k];
					s[k] = s[k + 1];
					s[k + 1] = t;
					if (k < n - 1) {
						for (int i = 0; i < n; i++) {
							t = V.getQuick(i, k + 1);
							V.setQuick(i, k + 1, V.getQuick(i, k));
							V.setQuick(i, k, t);
						}
					}
					if (k < m - 1) {
						for (int i = 0; i < m; i++) {
							t = U.getQuick(i, k + 1);
							U.setQuick(i, k + 1, U.getQuick(i, k));
							U.setQuick(i, k, t);
						}
					}
					k++;
				}
				iter = 0;
				p--;
			}
				break;
			}
		}
	}

	DoubleMatrix2D getPca(DoubleMatrix2D U, int userId1, int userId2, int itemId1, int itemId2) {
		DoubleMatrix2D pca = new DenseDoubleMatrix2D(userId2 - userId1 + 1, itemId2 - itemId1 + 1);
		for (int u = userId1; u <= userId2; u++) {
			for (int i = itemId1; i <= itemId2; i++) {
				pca.setQuick(u - userId1, i - itemId1, U.getQuick(u, i));
			}
		}
		return pca;
	}

	public DoubleMatrix2D getU() {
		return U;
	}

	public double[] getS() {
		return s;
	}

	/*public DoubleMatrix2D getSingular() {
		singular = new DenseDoubleMatrix2D(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				singular.setQuick(i, j, 0d);
			}
			singular.setQuick(i, i, s[i]);
		}
		return singular;
	}

	public double norm2() {
		return s[0];
	}

	public double cond() {
		return s[0] / s[Math.min(m, n) - 1];
	}

	public int rank() {
		double eps = Math.pow(2, -52);
		double tol = Math.max(m, n) * s[0] * eps;
		int r = 0;
		for (int i = 0; i < s.length; i++) {
			if (s[i] > tol) {
				r++;
			}
		}
		return r;
	}*/


	
	/*public double getRating(int user_id, int item_id){
		double rating = 0;
		for (int index : indicesByUser.get(user_id)) {
			if (item_id == A.getItem(index)) {
				rating = A.getRating(index);
			}
		}
		return rating;
	}*/
	
	/*public void setRating(int user_id, int item_id, double newRating) {
		for (int index : indicesByUser.get(user_id)) {
			if (item_id == A.getItem(index)) {
				A.setRating(index, newRating);
			}
		}
	}*/

	private static final long serialVersionUID = 1L;
}
