<?php

use Illuminate\Http\Request;
use Illuminate\Support\Facades\Route;
use App\Http\Controllers\VenteController;

// Route pour récupérer l'utilisateur authentifié
Route::middleware('auth:sanctum')->get('/user', function (Request $request) {
    return $request->user();
});
Route::apiResource('ventes', VenteController::class)->parameters([
    'ventes' => 'numProduit'
]);
Route::get('stats/ventes', [VenteController::class, 'stats']);