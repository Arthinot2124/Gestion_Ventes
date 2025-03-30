<?php
namespace App\Http\Controllers;

use App\Models\Vente;
use Illuminate\Http\Request;

class VenteController extends Controller
{

    public function show($id)
    {
        $vente = Vente::find($id);

        // Si la vente n'existe pas, retourner une erreur 404
        if (!$vente) {
            return response()->json(['message' => 'Vente non trouvée'], 404);
        }

        // Retourner la vente en JSON
        return response()->json($vente);
    }

    // Afficher toutes les ventes
    public function index()
    {
        $ventes = Vente::all();
        return response()->json($ventes);
    }

    // Ajouter une vente
    public function store(Request $request)
    {
        $request->validate([
            'numProduit' => 'required|string',
            'design' => 'required|string',
            'prix' => 'required|numeric|min:0',
            'quantite' => 'required|integer|min:1',
        ]);

        $vente = Vente::create($request->all());
        return response()->json($vente, 201);
    }

    // Modifier une vente
    public function update(Request $request, $numProduit)
    {
        // Trouver la vente par numProduit au lieu de l'id
        $vente = Vente::where('numProduit', $numProduit)->firstOrFail();
        $vente->update($request->all());
        return response()->json($vente, 200);
    }

    // Supprimer une vente
    public function destroy($numProduit)
    {
        // Supprimer la vente par numProduit au lieu de l'id
        Vente::where('numProduit', $numProduit)->delete();
        return response()->json(null, 204);
    }

    // Calculer les statistiques
    public function stats()
    {
        $stats = Vente::selectRaw('MIN(prix * quantite) as min, MAX(prix * quantite) as max, SUM(prix * quantite) as total')
                      ->first();
    
        if (!$stats || ($stats->min === null && $stats->max === null && $stats->total === null)) {
            return response()->json(['message' => 'Aucune vente trouvée'], 404);
        }
    
        return response()->json($stats);
    }
    

   
}