
Pragmatic Indoor Localization with Quantum Computing
This repository accompanies the paper and presentation:
Pragmatic Indoor Localization with Quantum Computing
Presented at IEEE WF‑IoT 2025
Authors: B. H. Kee, C. K. Seow, D. M. S. Baena, J. H. J. Lee
GitHub Repository: https://github.com/LawrenceCKS/Quantumlocalisation


1. Motivation and Problem Statement
Indoor localisation is critical for applications such as:

Homeland security and rescue operations
Intelligent transportation systems in GPS‑denied environments
Freight, depot management, retail, and social IoT services
However:

GPS signals cannot penetrate indoor environments
Classical Wi‑Fi RSS fingerprinting suffers from:High computational complexity
Large storage requirements
Instability due to RSS fluctuations
Classical fingerprint matching requires searching for similarity between online and offline RSS measurements with complexity:
O(MN) where M is the number of fingerprints and N is the fingerprint dimension


2. Core Idea and Novelty
This work proposes a quantum fingerprinting approach for indoor Wi‑Fi RSS localisation that:

Reduces complexity from O(MN) to O(log(MN))
Exploits quantum superposition to compare all fingerprints in parallel
Uses a Swap Test to compute cosine similarity between fingerprints
Implements a first‑of‑its‑kind indoor localisation quantum circuit in IBM Qiskit
Key Innovations 

a)Eigenspace encoding of RSS measurements
  i)Dimensionality reduction from M APs → 2 Principal Components (PCs)
b)Centralisation of PCs to maximise fingerprint angular separation
  i) Constructive interference for correlated fingerprints
  ii)Destructive interference for uncorrelated fingerprints


3. Experimental Environment (Offline Phase)
The experimental setup strictly follows the IEEE WF‑IoT slides:

Coverage area: 6.6 m × 3.6 m
Grid size: 66 regions (11 × 6)
Tile size: 60 cm × 60 cm
Number of Wi‑Fi Access Points: M = 4
Samples per region: K = 1000 RSS measurements
RSS data is averaged in the linear (mW) domain before converting back to dBm to reduce noise.


4. RSS Eigenspace Encoding
4.1 PCA‑based Dimensionality Reduction

Eigen‑decomposition is applied to the RSS covariance matrix
RSS fingerprints are projected from M = 4 → 2 Principal Components
Eigenvectors are orthogonal, naturally mapping to qubit orthogonality
This makes the data suitable for amplitude encoding on quantum states.
4.2 Eigenspace Centrality
Before quantum encoding, the PCs are centralised:

Mean of all fingerprint PCs is subtracted
Angular separation between fingerprints is maximised
Improves Swap‑Test sensitivity
This step is critical for enhancing quantum interference effects.


5. Quantum Computing Background (Minimal)

Classical bit: discrete 0 or 1
Quantum bit (qubit): superposition of |0⟩ and |1⟩
Entanglement: correlation between qubits regardless of distance
These properties enable parallel similarity evaluation across fingerprints.


6. Proposed Quantum Localisation Algorithm
The localisation pipeline follows the 8‑stage process exactly as shown in the IEEE WF‑IoT paper:

Step 1: Initialisation
Initialise qubits for:

Address
Offline RSS data
Online RSS test data
Ancilla
Step 2 : Address Superposition
Hadamard gates place all address qubits in superposition → all reference locations exist simultaneously

Step 3: Address Routing
Control lines activate one route per reference location

Step 4: Offline RSS Encoding
Controlled‑U gates encode PC‑based fingerprints in parallel
→ First entanglement between location and fingerprint

Step 5: Online RSS Encoding
Test RSS vector encoded into test qubits

Step 6: Swap Test (CSWAP)
Ancilla‑controlled swap between offline and online data
→ Second entanglement across all qubits

Step 7: Quantum Interference
Hadamard gate creates constructive/destructive interference based on similarity

Step 8 : Measurement
Address qubits are measured to obtain probability distribution over locations


7. Qiskit Implementation
The implementation is structured into four logical blocks, matching the slides:

Superposition – addressing, routing, amplitude encoding
Comparison – Swap Test for cosine similarity
Entanglement – storing basis states as quantum memory
Measurement – probability distribution analysis
The circuit compares one online test vector against eight offline fingerprints in parallel.


8. Results and Analysis
8.1 Probability Distribution

Large number of shots (up to 52,428,800) stabilises the distribution
Correct region typically has the highest probability
Second‑highest probability often corresponds to spatially adjacent regions
8.2 Accuracy and Confusion Matrix

Accuracy improves monotonically with increasing shots
Confusion matrix confirms spatial consistency
8.3 Real Quantum Hardware

Experiments on actual IBM quantum devices show:Noise‑filtered results outperform unfiltered ones
Hardware noise remains a limiting factor


9. Limitations

Requires large number of shots for stable inference
Current quantum hardware suffers from:Decoherence
Gate errors
Limited qubit counts


10. Future Work (Aligned with Slides)

Scaling beyond 6+ qubits with richer environments
Larger experimental campaigns
Exploration of NLOS indoor localisation beyond RSS
Hybrid quantum‑classical localisation pipelines


11. Citation
If you use this work, please cite:
B. H. Kee, C. K. Seow, D. M. S. Baena, and J. H. J. Lee,
"Pragmatic Indoor Localization with Quantum Computing,"
IEEE World Forum on Internet of Things (WF‑IoT), 2025.




13. Contact

Assoc. Prof. Chee Kiat Seow
cheeKiat.seow@glasgow.ac.uk

